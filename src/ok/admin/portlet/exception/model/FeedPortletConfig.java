package ok.admin.portlet.exception.model;

import java.util.EnumMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.Collectors;

public class FeedPortletConfig {
    public static final Integer CHUNK_SIZE_DEFAULT = 28;

    private final EnumMap<PlatformType, PlatformPortletConfig> configs = new EnumMap<>(PlatformType.class);

    public FeedPortletConfig(NavigableMap<Integer, PortletByPosition> portletsByPosition, List<PlatformType> platformTypes) {
        for (PlatformType platform : platformTypes) {
            configs.put(platform, new PlatformPortletConfig(platform, CHUNK_SIZE_DEFAULT, portletsByPosition));
        }
    }

    public PlatformPortletConfig getConfig(PlatformType platform) {
        return configs.get(platform);
    }

    @Override
    public String toString() {
        return configs.values().stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n"));
    }
}

