package ok.admin.portlet;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.druid.granularity.QueryGranularity;
import io.druid.query.Result;
import io.druid.query.aggregation.AggregatorFactory;
import io.druid.query.aggregation.DoubleSumAggregatorFactory;
import io.druid.query.filter.DimFilter;
import io.druid.query.filter.DimFilters;
import io.druid.query.filter.RegexDimFilter;
import io.druid.query.timeseries.TimeseriesResultValue;
import ok.admin.rest.component.portlet.config.DruidDataLoaderConfig;
import ok.admin.rest.component.portlet.druid.DruidDataLoader;
import ok.admin.rest.component.portlet.model.PortletStatRecord;
import ok.admin.rest.component.portlet.model.PortletStatsResponse;
import one.comp.feed.portlet.PortletType;
import ru.ok.druid.client.util.OkQueryUtil;

@Component
public class PortletStatService {
    private final DruidDataLoader druidDataLoader;
    private final DruidDataLoaderConfig config;

    @Autowired
    public PortletStatService(DruidDataLoader druidDataLoader, DruidDataLoaderConfig config) {
        this.druidDataLoader = druidDataLoader;
        this.config = config;
    }

    public PortletStatsResponse loadPortletMetrics(PortletType portletType) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;
        ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime oneAndHalfHoursAgo = now.minusMinutes(config.offsetMinutes());
        String recentHoursInterval = formatter.format(oneAndHalfHoursAgo) + "/" + formatter.format(now);
        ZonedDateTime sevenDaysAgo = now.minusDays(config.offsetDays());
        String last7DaysInterval = formatter.format(sevenDaysAgo) + "/" + formatter.format(now);

        String feedFeatureContent = portletType.getContent().name().toLowerCase();
        List<Result<TimeseriesResultValue>> recentShows =
                druidDataLoader.loadPortletShows(
                        recentHoursInterval,
                        createFeedFeatureFilters(feedFeatureContent,".*show.*"),
                        createPortletAggregators("calls"),
                        OkQueryUtil.GRANULARITY_5MIN);

        List<Result<TimeseriesResultValue>> recentClicks = druidDataLoader.loadPortletClicks(
                recentHoursInterval,
                createFeedFeatureFilters(feedFeatureContent, ".*click.*"),
                createPortletAggregators("calls"),
                OkQueryUtil.GRANULARITY_5MIN);

        List<Result<TimeseriesResultValue>> recentSkips = druidDataLoader.loadPortletSkips(
                portletType,
                recentHoursInterval,
                createPortletAggregators("failures"),
                OkQueryUtil.GRANULARITY_5MIN);

        List<Result<TimeseriesResultValue>> weekShows = druidDataLoader.loadPortletShows(
                last7DaysInterval,
                createFeedFeatureFilters(feedFeatureContent,".*show.*"),
                createPortletAggregators("calls"),
                QueryGranularity.DAY);

        List<Result<TimeseriesResultValue>> weekClicks = druidDataLoader.loadPortletClicks(
                last7DaysInterval,
                createFeedFeatureFilters(feedFeatureContent, ".*click.*"),
                createPortletAggregators("calls"),
                QueryGranularity.DAY);

        List<Result<TimeseriesResultValue>> weekSkips = druidDataLoader.loadPortletSkips(
                portletType,
                last7DaysInterval,
                createPortletAggregators("failures"),
                QueryGranularity.DAY);

        return new PortletStatsResponse(
                convertTimeseries(recentShows, recentClicks, recentSkips, config.recentRecordLimit()),
                convertTimeseries(weekShows, weekClicks, weekSkips, config.daysRecordLimit()));
    }

    private List<PortletStatRecord> convertTimeseries(List<Result<TimeseriesResultValue>> showList,
                                                      List<Result<TimeseriesResultValue>> clickList,
                                                      List<Result<TimeseriesResultValue>> skipList,
                                                      int recordLimit) {
        Set<DateTime> timestamps = showList.stream().map(Result::getTimestamp).collect(Collectors.toSet());
        Map<DateTime, TimeseriesResultValue> showsMap = convertToMap(showList);
        Map<DateTime, TimeseriesResultValue> clicksMap = convertToMap(clickList);
        Map<DateTime, TimeseriesResultValue> skipsMap = convertToMap(skipList);

        NavigableMap<DateTime, PortletStatRecord> resultMap = new TreeMap<>();
        for (DateTime timestamp : timestamps) {
            if (!showsMap.containsKey(timestamp) || !clicksMap.containsKey(timestamp) || !skipsMap.containsKey(timestamp)) {
                continue;
            }

            double shows = extractMetricValue(showsMap.get(timestamp), "calls");
            double clicks = extractMetricValue(clicksMap.get(timestamp), "calls");
            double skips = extractMetricValue(skipsMap.get(timestamp), "failures");
            double ctr = shows > 0 ? (clicks * 100.0 / shows) : 0;
            double skipRate = shows > 0 ? (skips * 100.0 / shows) : 0;
            PortletStatRecord portletStatRecord = new PortletStatRecord(timestamp.toLocalDateTime().toString(), shows, clicks, skips, ctr, skipRate);
            resultMap.put(timestamp, portletStatRecord);
        }
        List<PortletStatRecord> res = resultMap.values().stream().toList();
        return res.size() > recordLimit ? res.subList(res.size() - recordLimit, res.size()) : res;
    }

    private static double extractMetricValue(TimeseriesResultValue timeseriesResultValue, String name) {
        return timeseriesResultValue.getDoubleMetric(name);
    }

    private static Map<DateTime, TimeseriesResultValue> convertToMap(List<Result<TimeseriesResultValue>> metrics) {
        Map<DateTime, TimeseriesResultValue> res = new HashMap<>();
        for (Result<TimeseriesResultValue> metric : metrics) {
            res.put(metric.getTimestamp(), metric.getValue());
        }
        return res;
    }

    private static DimFilter createFeedFeatureFilters(String feedFeatureContent, String operation) {
        return DimFilters.and(new ArrayList<>(List.of(new RegexDimFilter("operation", operation),
                new RegexDimFilter("type", "131"),
                new RegexDimFilter("contents", feedFeatureContent))));
    }

    private static List<AggregatorFactory> createPortletAggregators(String fieldName) {
        return new ArrayList<>(List.of(new DoubleSumAggregatorFactory(fieldName, fieldName, "double")));
    }
}
