package ok.admin.portlet;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import odnoklassniki.category.client.CategoryServiceFactory;
import ok.admin.common.pms.PmsProperty;
import one.app.community.control.ejb.feed.portlets.inserter.config.PortletConfigResolverConfiguration;
import one.app.community.control.ejb.feed.portlets.inserter.config.PortletConfigResolverConverter;
import one.conf.IConfiguration;
import one.conf.dynamic.ConfigurationException;
import one.ejb.NotNull;
import one.ejb.Nullable;

@Component
public class InserterConfigProcessor {
    private static final String EXT_PROPERTY_REGEXP = "\\$\\{([^${}]*)}";
    private static final Pattern EXP_PROPERTY_PATTERN = Pattern.compile(EXT_PROPERTY_REGEXP);

    private final PortletConfigResolverConverter resolverParser;
    private final PortletManagerBot bot;

    @Autowired
    public InserterConfigProcessor(PortletManagerBot bot, IConfiguration configuration) throws ConfigurationException {
        this.resolverParser = new PortletConfigResolverConverter(CategoryServiceFactory.createUserConf(configuration));
        this.bot = bot;
    }

    @NotNull
    public Set<String> getEnabledConfigsByUser(long userId, @NotNull String host, @NotNull PmsProperty resolverConfig) {
        Validate.notNull(host, "cannot retrieve enabled configs if host is null");
        Validate.notNull(resolverConfig, "cannot retrieve enabled configs from null");

        String propertyValue = injectExtProperties(host, resolverConfig.getPropertyValue());
        List<PortletConfigResolverConfiguration> resolverConfigs = resolverParser.convert(propertyValue);
        Set<String> result = new LinkedHashSet<>();

        for (PortletConfigResolverConfiguration config : resolverConfigs) {
            if (config.getEnabledIds().isEnabled(userId)) {
                result.add(config.getKey());
            }
        }

        return result;
    }

    @NotNull
    public List<PortletConfigResolverConfiguration> getAllResolverConfigs(@NotNull String host, @NotNull String resolverConfig) {
        Validate.notNull(host, "cannot retrieve enabled configs if host is null");
        Validate.notNull(resolverConfig, "cannot retrieve enabled configs from null");

        String propertyValue = injectExtProperties(host, resolverConfig);
        return resolverParser.convert(propertyValue);
    }

    /**
     * Этот метод инжектит все вложенные настройки вида "${property-name}" в pmsPropertyValue
     */
    @NotNull
    public String injectExtProperties(@NotNull String hostName, @NotNull String pmsPropertyValue) {
        Validate.notNull(hostName, "host cannot be null");
        Validate.notNull(pmsPropertyValue, "pmsPropertyValue cannot be null");

        // кэш для плейсхолдеров, чтобы не делать каждый раз запрос в пмс
        // key = propertyName
        HashMap<String, String> pmsPropertyCache = new HashMap<>();
        return injectInternal(null, hostName, pmsPropertyValue, pmsPropertyCache);
    }

    @NotNull
    private String injectInternal(@Nullable String from, @NotNull String hostName, @NotNull String pmsPropertyValue, @NotNull Map<String, String> pmsPropertyCache) {
        Matcher matcher = EXP_PROPERTY_PATTERN.matcher(pmsPropertyValue);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = pmsPropertyCache.get(key);
            if (replacement == null) {
                Map<String, PmsProperty> injects = bot.getPmsProperty(key);
                replacement = bot.getConfigByHost(hostName, injects);
                pmsPropertyCache.put(key, replacement);
            }
            if (from != null && replacement.contains(from) && pmsPropertyCache.containsKey(key)) {
                throw new IllegalArgumentException("Failed to inject properties because of infinite recursion for properties " + from + " and " +  key);
            }
            replacement = injectInternal(key, hostName, replacement, pmsPropertyCache);

            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);

        return result.toString();
    }
}
