package ok.admin.portlet.config;

import one.conf.annotation.PropertyInfo;
import one.conf.dynamic.IPropertyListenerSupport;
import one.spring.conf.ConfPropertyBean;

@ConfPropertyBean("feed.portlet-manager.cache")
public interface PortletServiceCacheConfig extends IPropertyListenerSupport {

    @PropertyInfo(defaultValue = "false")
    boolean isEnabled();

    @PropertyInfo(defaultValue = "500")
    int getCacheSize();

    @PropertyInfo(defaultValue = "10")
    int getExpireAfterWriteMinutes();
}
