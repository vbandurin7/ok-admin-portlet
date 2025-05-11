package ok.admin.portlet.exception.config;

import one.conf.annotation.PropertyInfo;
import one.spring.conf.ConfPropertyBean;

@ConfPropertyBean("feed.portlet-manager.druid")
public interface DruidDataLoaderConfig {

    @PropertyInfo(defaultValue = "150")
    int offsetMinutes();

    @PropertyInfo (defaultValue = "18")
    int recentRecordLimit();

    @PropertyInfo(defaultValue = "7")
    int offsetDays();

    @PropertyInfo(defaultValue = "7")
    int daysRecordLimit();
}
