package unit.ok.admin.portlet;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.github.benmanes.caffeine.cache.Cache;

import ok.admin.common.pms.PmsProperty;
import ok.admin.rest.component.portlet.config.PortletManagerPmsConfiguration;
import ok.admin.rest.component.portlet.config.PortletServiceCacheConfig;
import ok.admin.rest.component.portlet.exception.ConfigurationNotFoundException;
import ok.admin.rest.component.portlet.model.Candidate;
import ok.admin.rest.component.portlet.model.FeedPortletConfig;
import ok.admin.rest.component.portlet.model.PlatformType;
import ok.admin.rest.component.portlet.model.PortletByPosition;
import ok.admin.rest.component.portlet.model.PortletConfigEntry;
import ok.admin.rest.component.portlet.model.PortletConfigRequest;
import one.app.community.control.ejb.feed.portlets.inserter.PortletCategory;
import one.app.community.control.ejb.feed.portlets.inserter.config.PortletConfigResolverConfiguration;
import one.comp.feed.portlet.PortletType;
import one.conf.converter.ConverterEnabledIds;

import static ok.admin.rest.component.portlet.PortletTestUtil.RESOLVER_CONFIG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PortletServiceTest {
    private static final String HOST = "feed-ejb.test.pc";
    private static final String INSERTER_CONFIG_NAME = "inserterTest";
    private static final String RESOLVER_CONFIG_NAME = "resolverTest";
    private static final String CONFIG_PREFIX = "inserter";
    private static final String ENABLED_IDS = "111,222,333,444,555";
    private static final Long USER_ID = 123L;

    @Mock
    PortletManagerBot bot;
    @Mock
    InserterConfigProcessor inserterConfigProcessor;
    @Mock
    PortletManagerPmsConfiguration portletManagerPmsConfiguration;
    @Mock
    PortletServiceCacheConfig cacheConfig;
    @Mock
    Cache<String, FeedPortletConfig> cache;
    @InjectMocks
    PortletService portletService;

    @Test
    public void getInserterConfigTest_configExist_expectedPlatformConfigReturned() {
        PlatformType platformType = PlatformType.ANDROID;

        when(portletManagerPmsConfiguration.inserterConfigPropertyName()).thenReturn(CONFIG_PREFIX);
        when(bot.getPmsProperty(CONFIG_PREFIX + "." + INSERTER_CONFIG_NAME))
                .thenReturn(Map.of(HOST, new PmsProperty(PortletTestUtil.INSERTER_CONFIG, 0L, Date.from(Instant.now()))));
        when(inserterConfigProcessor.injectExtProperties(HOST, PortletTestUtil.INSERTER_CONFIG)).thenReturn(PortletTestUtil.INSERTER_CONFIG);

        assertEquals(PortletTestUtil.INSERTER_CONFIG_ANDROID, portletService.getInserterConfig(HOST, INSERTER_CONFIG_NAME, platformType));
    }

    @Test
    public void getInserterConfigTest_configNotExist_exceptionThrown() {
        PlatformType platformType = PlatformType.ANDROID;

        when(portletManagerPmsConfiguration.inserterConfigPropertyName()).thenReturn(CONFIG_PREFIX);
        when(bot.getPmsProperty(CONFIG_PREFIX + "." + INSERTER_CONFIG_NAME))
                .thenReturn(Collections.emptyMap());

        assertThrows(ConfigurationNotFoundException.class, () -> portletService.getInserterConfig(HOST, INSERTER_CONFIG_NAME, platformType));
    }

    @Test
    public void getResolverConfigsByUser_noConfigsForUser_emptyMap() {
        PmsProperty resolverProperty = new PmsProperty(RESOLVER_CONFIG, 0L, Date.from(Instant.now()));

        when(portletManagerPmsConfiguration.resolverConfigsPropertyName()).thenReturn(RESOLVER_CONFIG_NAME);
        when(bot.getPmsProperty(RESOLVER_CONFIG_NAME)).thenReturn(Map.of(HOST, resolverProperty));
        when(inserterConfigProcessor.getEnabledConfigsByUser(USER_ID, HOST, resolverProperty)).thenReturn(Collections.emptySet());

        assertEquals(Collections.emptyMap(), portletService.getResolverConfigsByUser(USER_ID));
    }

    @Test
    public void getResolverConfigsByUser_userInTwoConfigs_twoConfigsReturned() {
        PmsProperty resolverProperty = new PmsProperty(RESOLVER_CONFIG, 0L, Date.from(Instant.now()));

        when(portletManagerPmsConfiguration.resolverConfigsPropertyName()).thenReturn(RESOLVER_CONFIG_NAME);
        when(bot.getPmsProperty(RESOLVER_CONFIG_NAME)).thenReturn(Map.of(HOST, resolverProperty));
        when(inserterConfigProcessor.getEnabledConfigsByUser(USER_ID, HOST, resolverProperty)).thenReturn(new LinkedHashSet<>(List.of("conf1", "conf2")));

        assertEquals(Map.of(HOST, List.of("conf1", "conf2")), portletService.getResolverConfigsByUser(USER_ID));
    }

    @Test
    public void generateFeedPortletConfig_twoPortlets_configReturned() {
        List<PlatformType> platforms = List.of(PlatformType.ANDROID, PlatformType.API);
        PortletConfigRequest request = new PortletConfigRequest(
                List.of(new PortletConfigEntry(-1, "DzenNews"), new PortletConfigEntry(5, "MoviesRecommendations")),
                platforms, false, Long.toString(USER_ID));
        PortletByPosition p1 = new PortletByPosition(-1, 0, List.of(new Candidate(PortletCategory.CUSTOM_PORTLET, "0-255", EnumSet.of(PortletType.DzenNews), 1)));
        PortletByPosition p2 = new PortletByPosition(5, 0, List.of(new Candidate(PortletCategory.CUSTOM_PORTLET, "0-255", EnumSet.of(PortletType.MoviesRecommendations), 1)));
        NavigableMap<Integer, PortletByPosition> expectedPortletsByPos = new TreeMap<>();
        expectedPortletsByPos.put(-1, p1);
        expectedPortletsByPos.put(5, p2);

        assertEquals(PortletConfigGenerator.serializeToString(expectedPortletsByPos, platforms), portletService.generateFeedPortletConfig(HOST, INSERTER_CONFIG_NAME, request));
    }

    @Test
    public void enableInserterConfig_configEnabled_originalConfig() {
        PmsProperty resolverProperty = new PmsProperty(RESOLVER_CONFIG, 0L, Date.from(Instant.now()));
        Map<String, PmsProperty> resolverProperties = Map.of(HOST, resolverProperty);
        String enabledResolver = INSERTER_CONFIG_NAME + 5;
        List<PortletConfigResolverConfiguration> allConfigs = new ArrayList<>();
        addResolvers(allConfigs, 0, 5);
        allConfigs.add(resolver(enabledResolver, ENABLED_IDS + "," + USER_ID));
        addResolvers(allConfigs, 6, 10);

        when(portletManagerPmsConfiguration.inserterConfigPropertyName()).thenReturn(CONFIG_PREFIX);
        when(bot.getPmsProperty(CONFIG_PREFIX + "." + enabledResolver))
                .thenReturn(Map.of(HOST, new PmsProperty(PortletTestUtil.INSERTER_CONFIG, 0L, Date.from(Instant.now()))));
        when(portletManagerPmsConfiguration.resolverConfigsPropertyName()).thenReturn(enabledResolver);
        when(bot.getPmsProperty(enabledResolver)).thenReturn(resolverProperties);
        when(bot.getConfigByHost(HOST, resolverProperties)).thenReturn(RESOLVER_CONFIG);
        when(inserterConfigProcessor.injectExtProperties(HOST, PortletTestUtil.INSERTER_CONFIG)).thenReturn(PortletTestUtil.INSERTER_CONFIG);
        when(inserterConfigProcessor.injectExtProperties(HOST, RESOLVER_CONFIG)).thenReturn(RESOLVER_CONFIG);
        when(inserterConfigProcessor.getAllResolverConfigs(HOST, RESOLVER_CONFIG)).thenReturn(allConfigs);

        assertEquals(enabledResolver, portletService.enableInserterConfig(USER_ID, HOST, enabledResolver));
    }

    @Test
    public void enableInserterConfig_configEnabledNotFirst_newConfigName() {
        PmsProperty resolverProperty = new PmsProperty(RESOLVER_CONFIG, 0L, Date.from(Instant.now()));
        Map<String, PmsProperty> resolverProperties = Map.of(HOST, resolverProperty);
        String firstEnabledResolver = INSERTER_CONFIG_NAME + 2;
        String enabledResolver = INSERTER_CONFIG_NAME + 5;
        List<PortletConfigResolverConfiguration> allConfigs = new ArrayList<>();
        addResolvers(allConfigs, 0, 2);
        allConfigs.add(resolver(firstEnabledResolver, ENABLED_IDS + "," + USER_ID));
        addResolvers(allConfigs, 3, 5);
        allConfigs.add(resolver(enabledResolver, ENABLED_IDS + "," + USER_ID));
        addResolvers(allConfigs, 6, 10);

        when(portletManagerPmsConfiguration.inserterConfigPropertyName()).thenReturn(CONFIG_PREFIX);
        when(bot.getPmsProperty(CONFIG_PREFIX + "." + enabledResolver))
                .thenReturn(Map.of(HOST, new PmsProperty(PortletTestUtil.INSERTER_CONFIG, 0L, Date.from(Instant.now()))));
        when(portletManagerPmsConfiguration.resolverConfigsPropertyName()).thenReturn(enabledResolver);
        when(bot.getPmsProperty(enabledResolver)).thenReturn(resolverProperties);
        when(bot.getConfigByHost(HOST, resolverProperties)).thenReturn(RESOLVER_CONFIG);
        when(inserterConfigProcessor.injectExtProperties(HOST, PortletTestUtil.INSERTER_CONFIG)).thenReturn(PortletTestUtil.INSERTER_CONFIG);
        when(inserterConfigProcessor.injectExtProperties(HOST, RESOLVER_CONFIG)).thenReturn(RESOLVER_CONFIG);
        when(inserterConfigProcessor.getAllResolverConfigs(HOST, RESOLVER_CONFIG)).thenReturn(allConfigs);

        assertEquals(enabledResolver + "_g0", portletService.enableInserterConfig(USER_ID, HOST, enabledResolver));
    }

    @Test
    public void enableInserterConfig_configNotEnabled_enableConfig() {
        PmsProperty resolverProperty = new PmsProperty(RESOLVER_CONFIG, 0L, Date.from(Instant.now()));
        Map<String, PmsProperty> resolverProperties = Map.of(HOST, resolverProperty);
        String resolverToEnable = INSERTER_CONFIG_NAME + 5;
        List<PortletConfigResolverConfiguration> allConfigs = new ArrayList<>();
        addResolvers(allConfigs, 0, 10);

        when(portletManagerPmsConfiguration.inserterConfigPropertyName()).thenReturn(CONFIG_PREFIX);
        when(bot.getPmsProperty(CONFIG_PREFIX + "." + resolverToEnable))
                .thenReturn(Map.of(HOST, new PmsProperty(PortletTestUtil.INSERTER_CONFIG, 0L, Date.from(Instant.now()))));
        when(portletManagerPmsConfiguration.resolverConfigsPropertyName()).thenReturn(resolverToEnable);
        when(bot.getPmsProperty(resolverToEnable)).thenReturn(resolverProperties);
        when(bot.getConfigByHost(HOST, resolverProperties)).thenReturn(RESOLVER_CONFIG);
        when(inserterConfigProcessor.injectExtProperties(HOST, PortletTestUtil.INSERTER_CONFIG)).thenReturn(PortletTestUtil.INSERTER_CONFIG);
        when(inserterConfigProcessor.injectExtProperties(HOST, RESOLVER_CONFIG)).thenReturn(RESOLVER_CONFIG);
        when(inserterConfigProcessor.getAllResolverConfigs(HOST, RESOLVER_CONFIG)).thenReturn(allConfigs);

        assertEquals(resolverToEnable, portletService.enableInserterConfig(USER_ID, HOST, resolverToEnable));
    }

    @Test
    public void enableInserterConfig_configNotExist_enableConfig() {
        PmsProperty resolverProperty = new PmsProperty(RESOLVER_CONFIG, 0L, Date.from(Instant.now()));
        Map<String, PmsProperty> resolverProperties = Map.of(HOST, resolverProperty);
        String resolverToEnable = INSERTER_CONFIG_NAME + 10;
        List<PortletConfigResolverConfiguration> allConfigs = new ArrayList<>();
        addResolvers(allConfigs, 0, 10);

        when(portletManagerPmsConfiguration.inserterConfigPropertyName()).thenReturn(CONFIG_PREFIX);
        when(bot.getPmsProperty(CONFIG_PREFIX + "." + resolverToEnable))
                .thenReturn(Map.of(HOST, new PmsProperty(PortletTestUtil.INSERTER_CONFIG, 0L, Date.from(Instant.now()))));
        when(portletManagerPmsConfiguration.resolverConfigsPropertyName()).thenReturn(resolverToEnable);
        when(bot.getPmsProperty(resolverToEnable)).thenReturn(resolverProperties);
        when(bot.getConfigByHost(HOST, resolverProperties)).thenReturn(RESOLVER_CONFIG);
        when(inserterConfigProcessor.injectExtProperties(HOST, PortletTestUtil.INSERTER_CONFIG)).thenReturn(PortletTestUtil.INSERTER_CONFIG);
        when(inserterConfigProcessor.injectExtProperties(HOST, RESOLVER_CONFIG)).thenReturn(RESOLVER_CONFIG);
        when(inserterConfigProcessor.getAllResolverConfigs(HOST, RESOLVER_CONFIG)).thenReturn(allConfigs);

        assertEquals(resolverToEnable, portletService.enableInserterConfig(USER_ID, HOST, resolverToEnable));
    }

    private static void addResolvers(List<PortletConfigResolverConfiguration> configs, int from, int to) {
        for (int i = from; i < to; i++) {
            configs.add(resolver(INSERTER_CONFIG_NAME + i, ENABLED_IDS));
        }
    }

    private static PortletConfigResolverConfiguration resolver(String name, String enabledIds) {
        return new PortletConfigResolverConfiguration(
                name,
                ConverterEnabledIds.INSTANCE.convert(enabledIds),
                null, null, null, null, null, null, null);
    }
}
