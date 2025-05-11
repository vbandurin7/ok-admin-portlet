package ok.admin.portlet.exception.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PortletConfigEntry {

    @JsonSerialize(using = ToStringSerializer.class)
    private final int position;
    private final String portletType;

    @JsonCreator
    public PortletConfigEntry(@JsonProperty("position") Integer position,
                              @JsonProperty("portletType") String portletType) {
        this.position = position == null ? 0 : position;
        this.portletType = portletType;
    }

    public String getPortletType() {
        return portletType;
    }

    public int getPosition() {
        return position;
    }
}
