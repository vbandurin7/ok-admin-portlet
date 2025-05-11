package ok.admin.portlet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import ok.admin.common.pms.PmsProperty;
import ok.admin.rest.component.portlet.config.PortletManagerPmsConfiguration;
import ok.admin.rest.component.portlet.config.PortletServiceCacheConfig;
import ok.admin.rest.component.portlet.exception.ConfigurationNotFoundException;
import ok.admin.rest.component.portlet.exception.InvalidConfigurationException;
import ok.admin.rest.component.portlet.model.FeedPortletConfig;
import ok.admin.rest.component.portlet.model.PlatformType;
import ok.admin.rest.component.portlet.model.PortletByPosition;
import ok.admin.rest.component.portlet.model.PortletConfigRequest;
import one.app.community.conf.AbstractResolverConfiguration;
import one.app.community.control.ejb.feed.portlets.inserter.config.PortletConfigResolverConfiguration;
import one.conf.converter.ConverterEnabledIds;
import one.util.StringUtil;

@Component
public class PortletService {
    private final PortletManagerBot bot;
    private final InserterConfigProcessor inserterConfigProcessor;
    private final PortletManagerPmsConfiguration portletManagerPmsConfiguration;
    private final PortletServiceCacheConfig cacheConfig;
    private Cache<String, FeedPortletConfig> cache;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public PortletService(PortletManagerBot bot,
                          InserterConfigProcessor inserterConfigProcessor,
                          PortletManagerPmsConfiguration portletManagerPmsConfiguration,
                          PortletServiceCacheConfig cacheConfig) {
        this.bot = bot;
        this.portletManagerPmsConfiguration = portletManagerPmsConfiguration;
        this.inserterConfigProcessor = inserterConfigProcessor;
        this.cacheConfig = cacheConfig;
        this.cacheConfig.addPropertyListener(key -> configureCache());
    }

    public Map<String, List<String>> getResolverConfigsByUser(long userId) {
        Map<String, PmsProperty> resolverConfigs = getResolverProperties();
        return getResolverConfigsInternal(userId, resolverConfigs);
    }

    public String getInserterConfig(String host, String configName, PlatformType platformType) {
        Map<String, PmsProperty> inserterConfigs = getInserterConfigs(configName);
        if (inserterConfigs.get(host) == null) {
            throw new ConfigurationNotFoundException("Inserter config [host=" + host + "; configName=" + configName + "] not found");
        }

        String inserterConfig = inserterConfigProcessor.injectExtProperties(host, inserterConfigs.get(host).getPropertyValue());

        return extractPlatformConfig(inserterConfig, host, configName, platformType);
    }

    public String generateFeedPortletConfig(String host, String configName, PortletConfigRequest portletConfigRequest) {
        NavigableMap<Integer, PortletByPosition> portletsByPosition = PortletConfigGenerator.generateConfig(portletConfigRequest.getPortlets());
        String portletInserterConfig = PortletConfigGenerator.serializeToString(portletsByPosition, portletConfigRequest.getPlatformTypes());
        updateOrCreateInserterConfig(host, configName, portletInserterConfig);
        return portletInserterConfig;
    }

    public String enableInserterConfig(long userId, String host, String inserterConfigToEnable) {
        String inserterConfig = getInserterConfig(host, inserterConfigToEnable, PlatformType.ALL);
        if (inserterConfig == null) {
            throw new ConfigurationNotFoundException("Inserter config [host=" + host + "; config=" + inserterConfigToEnable + "; not found");
        }

        String resolvers = inserterConfigProcessor.injectExtProperties(host, bot.getConfigByHost(host, getResolverProperties()));
        if (StringUtil.isEmpty(resolvers)) {
            updateOrCreateResolverConfig(host, insertAtTop(resolvers, userId, inserterConfigToEnable));
            return inserterConfigToEnable;
        }
        List<PortletConfigResolverConfiguration> allConfigs = inserterConfigProcessor.getAllResolverConfigs(host, resolvers);
        for (PortletConfigResolverConfiguration config : allConfigs) {
            if (config.getEnabledIds().isEnabled(userId)) {
                if (config.getKey().equals(inserterConfigToEnable)) {
                    // конфиг уже включен
                    return inserterConfigToEnable;
                }

                // запрашиваемый конфиг не первый включенный => надо сгенерировать новый и включить
                HashSet<String> resolverConfigKeys = allConfigs.stream()
                        .map(AbstractResolverConfiguration::getKey)
                        .collect(Collectors.toCollection(HashSet::new));
                String finalInserterConfigName = getNextConfigName(inserterConfigToEnable, resolverConfigKeys);
                // todo: add exception handling
                updateOrCreateInserterConfig(host, finalInserterConfigName, inserterConfig);
                updateOrCreateResolverConfig(host, insertAtTop(resolvers, userId, finalInserterConfigName));
                return finalInserterConfigName;
            }

            // До этого момента не включен ни один конфиг, можно просто прописать userId.
            if (config.getKey().equals(inserterConfigToEnable)) {
                updateOrCreateResolverConfig(host, insertInline(resolvers, userId, inserterConfigToEnable));
                return inserterConfigToEnable;
            }
        }
        // на пользователя ничего не включено и конфига не существует
        updateOrCreateResolverConfig(host, insertAtTop(resolvers, userId, inserterConfigToEnable));
        return inserterConfigToEnable;
    }


    public void disableInserterConfig(long userId, String host, String inserterConfigName) {
        String resolvers = inserterConfigProcessor.injectExtProperties(host, bot.getConfigByHost(host, getResolverProperties()));
        if (StringUtil.isEmpty(resolvers)) {
            return;
        }

        String resolverPrefix = "key." + inserterConfigName + "=";
        String[] resolverConfigs = resolvers.split(System.lineSeparator());
        for (int i = 0; i < resolverConfigs.length; i++) {
            if (!resolverConfigs[i].startsWith(resolverPrefix)) {
                continue;
            }
            String enabledIds = resolverConfigs[i].split("=")[1];
            if (!ConverterEnabledIds.INSTANCE.convert(enabledIds).isEnabled(userId)) {
                break;
            }
            if (enabledIds.contains(Long.toString(userId))) {
                String[] idsArray = enabledIds.split(",");
                if (idsArray.length == 1) {
                    resolverConfigs[i] = null;
                    break;
                }
                enabledIds = Arrays.stream(idsArray)
                        .filter(id -> id.equals(Long.toString(userId)))
                        .collect(Collectors.joining(","));
            } else {
                enabledIds += ",-ID:" + userId;
            }
            resolverConfigs[i] = resolverPrefix + enabledIds;
            break;
        }
        String resultResolvers = Arrays.stream(resolverConfigs).filter(Objects::nonNull).collect(Collectors.joining(System.lineSeparator()));
        updateOrCreateResolverConfig(host, resultResolvers);
    }

    private static String insertAtTop(String resolversConfig, long userId, String inserterConfigToEnable) {
        StringBuilder sb = new StringBuilder(resolversConfig);
        int firstResolver = Math.max(0, sb.indexOf("key."));
        sb.insert(firstResolver, "key." + inserterConfigToEnable + "=" + userId + System.lineSeparator());
        return sb.toString();
    }

    private static String insertInline(String resolversConfig, long userId, String inserterConfigToEnable) {
        StringBuilder sb = new StringBuilder(resolversConfig);
        String resolverPrefix = "key." + inserterConfigToEnable + "=";
        int resolverToAppendPosition = sb.indexOf(resolverPrefix);
        sb.insert(resolverToAppendPosition + resolverPrefix.length(), userId + ",");
        return sb.toString();
    }

    private static String getNextConfigName(String configToEnable, Set<String> allConfigs) {
        String result = configToEnable;
        for (int i = 0; allConfigs.contains(result); i++) {
            result = configToEnable + "_g" + i;
        }
        return result;
    }

    private Map<String, List<String>> getResolverConfigsInternal(long userId, Map<String, PmsProperty> resolverConfigs) {
        Map<String, List<String>> userResolverConfigsByHost = new HashMap<>();
        for (Map.Entry<String, PmsProperty> hostResolverEntry : resolverConfigs.entrySet()) {
            List<String> enabledConfigs =
                    inserterConfigProcessor.getEnabledConfigsByUser(
                            userId,
                            hostResolverEntry.getKey(),
                            hostResolverEntry.getValue()
                    ).stream().toList();
            if (!enabledConfigs.isEmpty()) {
                userResolverConfigsByHost.put(hostResolverEntry.getKey(), enabledConfigs);
            }
        }
        return userResolverConfigsByHost;
    }

    private static String extractPlatformConfig(String inserterConfig, String host, String configName, PlatformType platformType) {
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
        return bot.getPmsProperty(getFullInserterConfigName(configName));
    }

    private void updateOrCreateInserterConfig(String host, String configName, String value) {
        bot.updatePmsProperty(host, getFullInserterConfigName(configName), value);
    }

    private void updateOrCreateResolverConfig(String host, String value) {
        bot.updatePmsProperty(host, portletManagerPmsConfiguration.resolverConfigsPropertyName(), value);
    }

    private Map<String, PmsProperty> getResolverProperties() {
        return bot.getPmsProperty(portletManagerPmsConfiguration.resolverConfigsPropertyName());
    }

    private String getFullInserterConfigName(String configName) {
        return portletManagerPmsConfiguration.inserterConfigPropertyName()
                + (StringUtil.isEmpty(configName) ? "" : "." + configName);
    }

    private void configureCache() {
        if (!cacheConfig.isEnabled()) {
            this.cache = null;
            return;
        }

        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
                .maximumSize(cacheConfig.getCacheSize())
                .expireAfterWrite(cacheConfig.getExpireAfterWriteMinutes(), TimeUnit.MINUTES);

        Cache<String, FeedPortletConfig> tempCache = cacheBuilder.build();

        if (cache != null) {
            cache.asMap().forEach(tempCache::put);
        }
        this.cache = tempCache;
    }
}