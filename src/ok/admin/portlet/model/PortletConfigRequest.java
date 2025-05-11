package ok.admin.portlet.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PortletConfigRequest {
    private final List<PortletConfigEntry> portlets;
    private final List<PlatformType> platformTypes;
    private final boolean enableConfig;
    private final String userId;


    @JsonCreator
    public PortletConfigRequest(@JsonProperty("portlets") List<PortletConfigEntry> portlets,
                                @JsonProperty("platformTypes") List<PlatformType> platformTypes,
                                @JsonProperty("enableConfig") Boolean enableConfig,
                                @JsonProperty("userId") String userId) {
        this.portlets = portlets;
        this.platformTypes = platformTypes;
        this.enableConfig = enableConfig != null && enableConfig;
        this.userId = userId;
    }

    public List<PortletConfigEntry> getPortlets() {
        return portlets;
    }

    public List<PlatformType> getPlatformTypes() {
        return platformTypes;
    }

    public boolean isEnableConfig() {
        return enableConfig;
    }

    public String getUserId() {
        return userId;
    }
}