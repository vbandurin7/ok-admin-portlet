package ok.admin.portlet.model;

import java.util.NavigableMap;
import java.util.stream.Collectors;

public record PlatformPortletConfig(PlatformType platform, int chunkSize, NavigableMap<Integer, PortletByPosition> portletsByPosition) {

    @Override
    public String toString() {
        return """
                %s={
                   chunkSize=%d
                   portletsByPosition={
                        %s
                   }
                }
                """.formatted(platform, chunkSize, portletsByPosition.values().stream()
                .map(PortletByPosition::toString)
                .collect(Collectors.joining(",\n")));
    }
}