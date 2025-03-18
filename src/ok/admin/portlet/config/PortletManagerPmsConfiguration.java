package ok.admin.portlet.config;

import one.conf.annotation.PropertyInfo;
import one.spring.conf.ConfPropertyBean;

@ConfPropertyBean("feed.portlet-manager.configuration")
public interface PortletManagerPmsConfiguration {

    @PropertyInfo(defaultValue = "odnoklassniki-ejb")
    String applicationName();

    @PropertyInfo(defaultValue = "host-odnoklassniki-ejb")
    String commonHost();

    @PropertyInfo(defaultValue = "one.app.portlets.inserter.resolversConfigs")
    String resolverConfigsPropertyName();

    @PropertyInfo(defaultValue = "one.app.portlets.inserter.config")
    String inserterConfigPropertyName();

    @PropertyInfo(defaultValue = "one.app.portlets.inserter.lastSeenTargeting")
    String lastSeenTargetingPropertyName();

    @PropertyInfo
    String username();

    @PropertyInfo
    String password();
}
