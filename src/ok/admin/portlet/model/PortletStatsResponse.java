package ok.admin.portlet.model;

import java.util.List;

public class PortletStatsResponse {

    private List<PortletStatRecord> lastDayStats;
    private List<PortletStatRecord> lastWeekStats;

    public PortletStatsResponse() {
    }

    public PortletStatsResponse(List<PortletStatRecord> dayStats, List<PortletStatRecord> weekStats) {
        this.lastDayStats = dayStats;
        this.lastWeekStats = weekStats;
    }

    public List<PortletStatRecord> getLastDayStats() {
        return lastDayStats;
    }

    public void setLastDayStats(List<PortletStatRecord> lastDayStats) {
        this.lastDayStats = lastDayStats;
    }

    public List<PortletStatRecord> getLastWeekStats() {
        return lastWeekStats;
    }

    public void setLastWeekStats(List<PortletStatRecord> lastWeekStats) {
        this.lastWeekStats = lastWeekStats;
    }
}
