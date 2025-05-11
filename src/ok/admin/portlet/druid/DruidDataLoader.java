package ok.admin.portlet.druid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.druid.granularity.QueryGranularity;
import io.druid.query.Druids;
import io.druid.query.Result;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.filter.DimFilter;
import io.druid.query.filter.RegexDimFilter;
import io.druid.query.timeseries.TimeseriesQuery;
import io.druid.query.timeseries.TimeseriesResultValue;
import one.comp.feed.portlet.PortletType;
import ru.ok.druid.client.DruidClient;
import ru.ok.druid.client.DruidException;
import ru.ok.druid.client.util.OkQueryUtil;

@Component
public class DruidDataLoader {
    private static final String FEED_FEATURES_TABLE = "feedfeatureoperationsdivided";
    private static final String PORTLET_STAT_TABLE = "portletservicestat";

    private final DruidClient druidClient;

    @Autowired
    public DruidDataLoader(DruidClient druidClient) {
        this.druidClient = druidClient;
    }

    public List<Result<TimeseriesResultValue>> loadPortletSkips(PortletType portletType, String interval,
                                                                List<AggregatorFactory> aggs, QueryGranularity granularity) {
        return loadFromDruid(PORTLET_STAT_TABLE,
                new RegexDimFilter("operation", ".*feedskip." + portletType.getLowerCaseName() + ".*"),
                interval,
                aggs,
                granularity);
    }

    public List<Result<TimeseriesResultValue>> loadPortletShows(String interval, DimFilter filter,
                                                                List<AggregatorFactory> aggs, QueryGranularity granularity) {
        return loadFromDruid(FEED_FEATURES_TABLE,
                filter,
                interval,
                aggs,
                granularity);
    }

    public List<Result<TimeseriesResultValue>> loadPortletClicks(String interval, DimFilter filter,
                                                                 List<AggregatorFactory> aggs, QueryGranularity granularity) {
        return loadFromDruid(FEED_FEATURES_TABLE,
                filter,
                interval,
                aggs,
                granularity);
    }

    private List<Result<TimeseriesResultValue>> loadFromDruid(String table, DimFilter dimFilter,
                                                              String interval, List<AggregatorFactory> aggs,
                                                              QueryGranularity granularity) {
        TimeseriesQuery query = Druids.newTimeseriesQueryBuilder()
                .dataSource(table)
                .filters(dimFilter)
                .intervals(interval)
                .aggregators(aggs)
                .granularity(granularity)
                .build();
        try {
            return druidClient.queryTimeseries(query);
        } catch (DruidException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Result<TimeseriesResultValue>> get(String interval, DimFilter dimFilter, List<AggregatorFactory> aggregators) throws DruidException {
        TimeseriesQuery query = Druids.newTimeseriesQueryBuilder()
                .dataSource("TABLE")
                .filters(dimFilter)
                .intervals(interval)
                .aggregators(aggregators)
                .granularity(OkQueryUtil.GRANULARITY_5MIN)
                .build();
        return druidClient.queryTimeseries(query);
    }
}
