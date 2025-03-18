package ok.admin.portlet.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PortletConfigRequest {
    private final List<PortletConfigEntry> portlets;

    @JsonCreator
    public PortletConfigRequest(@JsonProperty("portlets") List<PortletConfigEntry> portlets) {
        this.portlets = portlets;
    }

    public List<PortletConfigEntry> getPortlets() {
        return portlets;
    }
}