package ok.admin.portlet.model;

import java.util.NavigableMap;

public class FeedPortletConfig {
    public static Integer CHUNK_SIZE_DEFAULT = 28;

    private final PlatformPortletConfig webConfig;
    private final PlatformPortletConfig mobConfig;
    private final PlatformPortletConfig apiConfig;
    private final PlatformPortletConfig androidConfig;

    public FeedPortletConfig(NavigableMap<Integer, PortletByPosition> portletsByPosition) {
        this.webConfig = new PlatformPortletConfig(PlatformType.WEB, CHUNK_SIZE_DEFAULT, portletsByPosition);
        this.mobConfig = new PlatformPortletConfig(PlatformType.MOB, CHUNK_SIZE_DEFAULT, portletsByPosition);
        this.apiConfig = new PlatformPortletConfig(PlatformType.API, CHUNK_SIZE_DEFAULT, portletsByPosition);
        this.androidConfig = new PlatformPortletConfig(PlatformType.ANDROID, CHUNK_SIZE_DEFAULT, portletsByPosition);
    }

    @Override
    public String toString() {
        return webConfig + "\n"
                + mobConfig + "\n"
                + apiConfig + "\n"
                + androidConfig;
    }
}
