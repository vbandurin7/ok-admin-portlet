package ok.admin.portlet.model;

import java.util.EnumSet;
import java.util.stream.Collectors;

import one.app.community.control.ejb.feed.portlets.inserter.PortletCategory;
import one.comp.feed.portlet.PortletType;

public record Candidate(PortletCategory category, String enabled, EnumSet<PortletType> candidates, double showProbability) {

    @Override
    public String toString() {
        return """
                {
                    category=%s
                    enabled=%s
                    candidates=%s
                    showProbability=%.2f
                }
                """.formatted(category, enabled, candidates.stream().map(Enum::toString).collect(Collectors.joining(",")), showProbability);
    }
}
