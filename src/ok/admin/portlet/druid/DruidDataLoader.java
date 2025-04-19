package ok.admin.rest.component.portlet.druid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.druid.query.aggregation.AggregatorFactory;
import ru.ok.druid.client.DruidClient;
import io.druid.query.Druids;
import io.druid.query.Result;
import io.druid.query.filter.DimFilter;
import io.druid.query.timeseries.TimeseriesQuery;
import io.druid.query.timeseries.TimeseriesResultValue;
import ru.ok.druid.client.DruidException;
import ru.ok.druid.client.util.OkQueryUtil;

@Component
public class DruidDataLoader {
    private static final String TABLE = "TABLE_NAME";

    private final DruidClient druidClient;


    @Autowired
    public DruidDataLoader(DruidClient druidClient) {
        this.druidClient = druidClient;
    }

    public List<Result<TimeseriesResultValue>> fetchMetrics(String interval, DimFilter dimFilter, List<AggregatorFactory> aggregators) throws DruidException {
        TimeseriesQuery query = Druids.newTimeseriesQueryBuilder()
                .dataSource(TABLE)
                .intervals(interval)
                .filters(dimFilter)
                .aggregators(aggregators)
                .granularity(OkQueryUtil.GRANULARITY_5MIN)
                .build();
        return druidClient.queryTimeseries(query);
    }
}
