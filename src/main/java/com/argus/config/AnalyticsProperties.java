package com.argus.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "argus.analytics")
public class AnalyticsProperties {

    @Min(60000)
    private long aggregationFixedDelayMillis = 300000;

    @Min(1)
    private int rawDataRetentionDays = 30;

    @Min(1)
    private int summaryRetentionDays = 365;

    @Min(1)
    private int reportRetentionDays = 365;

    @Min(60000)
    private long cleanupFixedDelayMillis = 3600000;

    @Min(60000)
    private long reportProcessingFixedDelayMillis = 60000;

    @Min(1)
    private int largeReportThresholdDays = 30;

    public long getAggregationFixedDelayMillis() {
        return aggregationFixedDelayMillis;
    }

    public void setAggregationFixedDelayMillis(long aggregationFixedDelayMillis) {
        this.aggregationFixedDelayMillis = aggregationFixedDelayMillis;
    }

    public int getRawDataRetentionDays() {
        return rawDataRetentionDays;
    }

    public void setRawDataRetentionDays(int rawDataRetentionDays) {
        this.rawDataRetentionDays = rawDataRetentionDays;
    }

    public int getSummaryRetentionDays() {
        return summaryRetentionDays;
    }

    public void setSummaryRetentionDays(int summaryRetentionDays) {
        this.summaryRetentionDays = summaryRetentionDays;
    }

    public int getReportRetentionDays() {
        return reportRetentionDays;
    }

    public void setReportRetentionDays(int reportRetentionDays) {
        this.reportRetentionDays = reportRetentionDays;
    }

    public long getCleanupFixedDelayMillis() {
        return cleanupFixedDelayMillis;
    }

    public void setCleanupFixedDelayMillis(long cleanupFixedDelayMillis) {
        this.cleanupFixedDelayMillis = cleanupFixedDelayMillis;
    }

    public long getReportProcessingFixedDelayMillis() {
        return reportProcessingFixedDelayMillis;
    }

    public void setReportProcessingFixedDelayMillis(long reportProcessingFixedDelayMillis) {
        this.reportProcessingFixedDelayMillis = reportProcessingFixedDelayMillis;
    }

    public int getLargeReportThresholdDays() {
        return largeReportThresholdDays;
    }

    public void setLargeReportThresholdDays(int largeReportThresholdDays) {
        this.largeReportThresholdDays = largeReportThresholdDays;
    }
}
