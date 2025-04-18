package ok.admin.portlet.druid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.druid.query.Druids;
import io.druid.query.Result;
import io.druid.query.aggregation.CountAggregatorFactory;
import io.druid.query.filter.DimFilters;
import io.druid.query.timeseries.TimeseriesQuery;
import io.druid.query.timeseries.TimeseriesResultValue;
import ru.ok.druid.client.DruidClient;
import ru.ok.druid.client.util.OkQueryUtil;

@Component
public class DruidDataLoader {

    private final DruidClient druidClient;


    @Autowired
    public DruidDataLoader(DruidClient druidClient) {
        this.druidClient = druidClient;
    }

    public void makeQuery() {
        TimeseriesQuery query = Druids.newTimeseriesQueryBuilder()
                .dataSource("FeedFeatureOperationsDruid")
                .intervals("2025-04-16T00:00:00.000Z/2025-04-17T00:00:00.000Z")
                .filters(
                        DimFilters.and(
                                DimFilters.regex("Operation", ".*show.*"),
                                DimFilters.dimEquals("Type", "131"),
                                DimFilters.regex("Partition", "1", "2", "3", "4")
                        )
                )
                .aggregators(List.of(
                        new CountAggregatorFactory("total_shows", null, "long")  // Используем нужный конструктор
                ))
                .granularity(OkQueryUtil.GRANULARITY_5MIN)
                .build();
        List<Result<TimeseriesResultValue>> results;
        try {
            results = druidClient.queryTimeseries(query);
        } catch (Exception e) {
            int b = 1;
        }
        int a = 123;
    }
}
