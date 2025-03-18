package ok.admin.portlet;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ok.admin.rest.component.portlet.model.PortletConfigEntry;
import one.app.community.control.ejb.feed.portlets.inserter.PortletCategory;
import one.app.community.control.ejb.feed.portlets.inserter.config.CustomPortletTaskConfig;
import one.app.community.control.ejb.feed.portlets.inserter.config.PortletClientConfig;
import one.app.community.control.ejb.feed.portlets.inserter.config.PortletConfig;
import one.app.community.control.ejb.feed.portlets.inserter.config.PortletConfigWithCandidates;
import one.comp.feed.portlet.PortletType;
import one.conf.converter.Converter;

public class PortletConfigGenerator {
    public static String ENABLED_DEFAULT = "0-255";
    public static Double SHOW_PROBABILITY_DEFAULT = 1.0d;
    public static Double EMPTY_PROBABILITY_DEFAULT = 0d;


    public static PortletClientConfig generateConfig(List<PortletConfigEntry> entries) {
        List<PortletConfig> portletConfigs = new ArrayList<>();
        for (PortletConfigEntry entry : entries) {
            CustomPortletTaskConfig portletConfig = new CustomPortletTaskConfig();
            portletConfig.setCategory(PortletCategory.CUSTOM_PORTLET);
            portletConfig.setPosition(entry.getPosition());
            portletConfig.setEnabled(Converter.ENABLED_IDS.convert(ENABLED_DEFAULT));
            portletConfig.setShowProbability(SHOW_PROBABILITY_DEFAULT);
            try {
                portletConfig.setCandidates(EnumSet.of(PortletType.valueOf(entry.getPortletType())));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("PortletType " + entry.getPortletType() + " not found", e);
            }
            portletConfigs.add(portletConfig);
        }

        Map<Integer, PortletConfigWithCandidates> portletsByPosition = new HashMap<>();
        portletConfigs.forEach(p ->
                portletsByPosition.computeIfAbsent(p.position(), k -> createPortletConfigWithCandidates(p.position())).getCandidates().add(p));
        PortletClientConfig portletClientConfig = new PortletClientConfig();
        setPrivateField(portletClientConfig, "portletConfigsAsPlainList", portletConfigs);
        setPrivateField(portletClientConfig, "portletsByPosition", portletsByPosition);
        return portletClientConfig;
    }

    private static PortletConfigWithCandidates createPortletConfigWithCandidates(int position) {
        PortletConfigWithCandidates portletConfigWithCandidates = new PortletConfigWithCandidates();
        setPrivateField(portletConfigWithCandidates, "position", position);
        setPrivateField(portletConfigWithCandidates, "emptyProbability", EMPTY_PROBABILITY_DEFAULT);
        setPrivateField(portletConfigWithCandidates, "candidates", new ArrayList<>());
        return portletConfigWithCandidates;
    }

    // Метод для установки значения приватного поля
    private static void setPrivateField(Object object, String fieldName, Object value){
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(object, value);
        } catch (IllegalAccessException e) {
            // todo обработать
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
}
