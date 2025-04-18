package ok.admin.portlet.druid;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import one.conf.IConfProperty;
import one.conf.IConfiguration;
import one.conf.converter.Converter;
import one.conf.converter.ListConverter;
import ru.ok.druid.client.DruidClient;
import ru.ok.druid.client.transport.HttpTransportFactory;

@Component
public class DataLoaderConfigurer {
    private final DruidClient druidClient;

    @Autowired
    public DataLoaderConfigurer(IConfiguration conf) {
        IConfProperty<List<String>> clusterConf = conf.getConfProperty("druid-cluster", new ListConverter<>(Converter.STRING, "\n\r"), Collections.singletonList("{1-3}.broker.druid.{dc,ec,kc,pc}.odkl.ru:8004"));

        druidClient = new DruidClient(HttpTransportFactory.clusterOneNio().create(clusterConf), conf.getHostName());
    }

    @Bean
    public DruidClient druidClient() {
        return druidClient;
    }
}

