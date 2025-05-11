package ok.admin.portlet.exception;

import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ok.admin.common.pms.PmsComponent;
import ok.admin.common.pms.PmsProperty;
import ok.admin.common.pms.PmsUpdateRequest;
import ok.admin.rest.component.pms.PmsConfig;
import ok.admin.rest.component.pms.PmsFactory;
import ok.admin.rest.component.portlet.exception.config.PortletManagerPmsConfiguration;
import one.ejb.NotNull;
import one.util.streamex.EntryStream;

/**
 * класс для чтения и обновления настроек портлетных конфигов
 * с помощью portlet-manager-bot в PMS
 */
@Component
public class PortletManagerBot {
    private static final String HOST_NAME_SEPARATOR = "\\.";
    private final PmsFactory pmsFactory;
    private final PmsConfig pmsConfig;
    private final PortletManagerPmsConfiguration portletManagerPmsConfiguration;
    private PmsComponent pmsComponent;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public PortletManagerBot(PmsFactory pmsFactory,
                             PmsConfig pmsConfig,
                             PortletManagerPmsConfiguration portletManagerPmsConfiguration) {
        this.pmsFactory = pmsFactory;
        this.pmsConfig = pmsConfig;
        this.portletManagerPmsConfiguration = portletManagerPmsConfiguration;
    }

    @PostConstruct
    private void init() {
        this.pmsComponent = pmsFactory.pmsComponent(pmsConfig);
    }

    @NotNull
    public String getConfigByHost(@NotNull String host, @NotNull Map<String, PmsProperty> allProperties) {
        PmsProperty result = allProperties.get(host);
        if (result != null) {
            return result.getPropertyValue();
        }

        if (Set.of(host.split(HOST_NAME_SEPARATOR)).contains("test")) {
            result = allProperties.get("test.clouds");
            if (result != null) {
                return result.getPropertyValue();
            }
        }

        result = allProperties.get(portletManagerPmsConfiguration.commonHost());
        return result == null ? "" : result.getPropertyValue();
    }

    public void updatePmsProperty(@NotNull String host, @NotNull String propertyName, @NotNull String value) {
        Map<String, PmsProperty> read = getPmsProperty(propertyName);
        Map<String, Long> updates = EntryStream
                .of(read)
                .mapValues(PmsProperty::getUpdateId)
                .toMap();
        PmsUpdateRequest request = new PmsUpdateRequest(
                portletManagerPmsConfiguration.applicationName(),
                host,
                propertyName,
                value,
                updates.getOrDefault(host, 0L)
        );
        pmsComponent.createOrUpdate(request, portletManagerPmsConfiguration.username(), portletManagerPmsConfiguration.password());
    }

    @NotNull
    public Map<String, PmsProperty> getPmsProperty(@NotNull String placeholder) {
        String[] split = placeholder.split("\\\\");
        String applicationName = split.length == 2 ? split[0] : portletManagerPmsConfiguration.applicationName();
        String propertyName = split.length == 2 ? split[1] : split[0];
        return pmsComponent.read(
                applicationName,
                null,
                propertyName,
                portletManagerPmsConfiguration.username(),
                portletManagerPmsConfiguration.password());
    }
}
