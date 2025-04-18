package ok.admin.portlet;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import ok.admin.rest.component.portlet.model.Candidate;
import ok.admin.rest.component.portlet.model.FeedPortletConfig;
import ok.admin.rest.component.portlet.model.PortletByPosition;
import ok.admin.rest.component.portlet.model.PortletConfigEntry;
import one.app.community.control.ejb.feed.portlets.inserter.PortletCategory;
import one.comp.feed.portlet.PortletType;

public class PortletConfigGenerator {
    public static String ENABLED_DEFAULT = "0-255";
    public static Double SHOW_PROBABILITY_DEFAULT = 1.0d;
    public static Double EMPTY_PROBABILITY_DEFAULT = 0d;

    public static NavigableMap<Integer, PortletByPosition> generateConfig(List<PortletConfigEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyNavigableMap();
        }
        TreeMap<Integer, PortletByPosition> portletsByPosition = new TreeMap<>();
        for (PortletConfigEntry entry : entries) {
            Candidate candidate;
            try {
                candidate = new Candidate(
                        PortletCategory.CUSTOM_PORTLET,
                        ENABLED_DEFAULT,
                        EnumSet.of(PortletType.valueOf(entry.getPortletType())),
                        SHOW_PROBABILITY_DEFAULT);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("PortletType " + entry.getPortletType() + " not found", e);
            }
            PortletByPosition portletByPosition = new PortletByPosition(entry.getPosition(), EMPTY_PROBABILITY_DEFAULT, List.of(candidate));
            portletsByPosition.put(entry.getPosition(), portletByPosition);
        }

        return portletsByPosition;
    }

    public static String serializeToString(NavigableMap<Integer, PortletByPosition> portletsByPosition) {
        FeedPortletConfig portletConfig = new FeedPortletConfig(portletsByPosition);
        return portletConfig.toString();
    }
}
