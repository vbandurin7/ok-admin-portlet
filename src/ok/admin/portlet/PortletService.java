package ok.admin.portlet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ok.admin.common.pms.PmsProperty;
import ok.admin.rest.component.portlet.config.PortletManagerPmsConfiguration;
import ok.admin.rest.component.portlet.exception.ConfigurationNotFoundException;
import ok.admin.rest.component.portlet.exception.InvalidConfigurationException;
import ok.admin.rest.component.portlet.model.PlatformType;

@Component
public class PortletService {
    private final PortletManagerBot bot;
    private final InserterConfigProcessor inserterConfigProcessor;
    private final PortletManagerPmsConfiguration portletManagerPmsConfiguration;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public PortletService(PortletManagerBot bot,
                          InserterConfigProcessor inserterConfigProcessor,
                          PortletManagerPmsConfiguration portletManagerPmsConfiguration) {
        this.bot = bot;
        this.portletManagerPmsConfiguration = portletManagerPmsConfiguration;
        this.inserterConfigProcessor = inserterConfigProcessor;
    }

    public Map<String, List<String>> getResolverConfigsByUser(long userId) {
        Map<String, PmsProperty> resolverConfigs = getResolverConfigs();

        Map<String, List<String>> userResolverConfigsByHost = new HashMap<>();
        for (Map.Entry<String, PmsProperty> hostResolverEntry : resolverConfigs.entrySet()) {
            List<String> enabledConfigs =
                    inserterConfigProcessor.getEnabledConfigsByUser(
                            userId,
                            hostResolverEntry.getKey(),
                            hostResolverEntry.getValue()
                    );
            if (!enabledConfigs.isEmpty()) {
                userResolverConfigsByHost.put(hostResolverEntry.getKey(), enabledConfigs);
            }
        }
        return userResolverConfigsByHost;
    }

    public String getFeedConfig(String host, String configName, PlatformType platformType) {
        Map<String, PmsProperty> inserterConfigs = getInserterConfigs(configName);
        if (inserterConfigs.get(host) == null) {
            throw new ConfigurationNotFoundException("Inserter config [host=" + host + "; configName=" + configName + "] not found");
        }

        String inserterConfig = inserterConfigProcessor.injectExtProperties(host, inserterConfigs.get(host).getPropertyValue());

        return extractPlatformConfig(inserterConfig, host, configName, platformType);
    }

    private String extractPlatformConfig(String inserterConfig, String host, String configName, PlatformType platformType) {
        if (platformType == PlatformType.ALL) {
            return inserterConfig;
        }
        int configStart = inserterConfig.indexOf(platformType + "=");
        if (configStart == -1) {
            throw new ConfigurationNotFoundException("Inserter config [host=" + host + "; configName=" + configName + "; platformType=" + platformType + "] not found");
        }
        int braceCounter = 1;
        for (int i = configStart + platformType.toString().length() + 2; i < inserterConfig.length(); i++) {
            char symbol = inserterConfig.charAt(i);
            if (symbol == '{') {
                braceCounter++;
            } else if (symbol == '}') {
                braceCounter--;
            }
            if (braceCounter == 0) {
                return inserterConfig.substring(configStart, i + 1);
            }
        }
        throw new InvalidConfigurationException("Invalid configuration, no closing '}' found");
    }

    private Map<String, PmsProperty> getInserterConfigs(String configName) {
        return bot.getPmsProperty(configName);
    }

    private Map<String, PmsProperty> getResolverConfigs() {
        return bot.getPmsProperty(portletManagerPmsConfiguration.resolverConfigsPropertyName());
    }

    private Map<String, PmsProperty> getLastSeenTargeting() {
        return bot.getPmsProperty(portletManagerPmsConfiguration.lastSeenTargetingPropertyName());
    }
}