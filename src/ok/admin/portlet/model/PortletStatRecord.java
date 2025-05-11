package ok.admin.portlet.model;

public class PortletStatRecord {
    private String timestampOrDay;
    private double impressions;
    private double clicks;
    private double skips;
    private double ctr;
    private double skipRate;

    public PortletStatRecord(String timestamp, double impressions, double clicks, double skips, double ctr, double skipRate) {
        this.timestampOrDay = timestamp; // Преобразуем в строку ISO-формата
        this.impressions = impressions;
        this.clicks = clicks;
        this.skips = skips;
        this.ctr = ctr;
        this.skipRate = skipRate;
    }

    public String getTimestampOrDay() {
        return timestampOrDay;
    }

    public void setTimestampOrDay(String timestampOrDay) {
        this.timestampOrDay = timestampOrDay;
    }

    public double getImpressions() {
        return impressions;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public double getClicks() {
        return clicks;
    }

    public void setClicks(int clicks) {
        this.clicks = clicks;
    }

    public double getSkips() {
        return skips;
    }

    public void setSkips(int skips) {
        this.skips = skips;
    }

    public double getCtr() {
        return ctr;
    }

    public void setCtr(double ctr) {
        this.ctr = ctr;
    }

    public double getSkipRate() {
        return skipRate;
    }

    public void setSkipRate(double skipRate) {
        this.skipRate = skipRate;
    }
}