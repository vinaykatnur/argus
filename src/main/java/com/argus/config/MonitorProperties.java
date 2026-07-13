package com.argus.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "argus.monitoring")
public class MonitorProperties {

    @Min(30)
    private int minimumIntervalSeconds;

    @Min(60)
    private int maximumIntervalSeconds;

    @Min(2)
    private int minimumFailureThreshold;

    @Min(2)
    private int defaultFailureThreshold;

    @Min(1)
    private int requestTimeoutSeconds;

    @Min(1)
    private int slowResponseThresholdMillis;

    @Min(1)
    @Max(100)
    private int workerPoolSize;

    @Min(1000)
    private long schedulerFixedDelayMillis;

    @Min(1)
    private int schedulerBatchSize;

    public int getMinimumIntervalSeconds() {
        return minimumIntervalSeconds;
    }

    public void setMinimumIntervalSeconds(int minimumIntervalSeconds) {
        this.minimumIntervalSeconds = minimumIntervalSeconds;
    }

    public int getMaximumIntervalSeconds() {
        return maximumIntervalSeconds;
    }

    public void setMaximumIntervalSeconds(int maximumIntervalSeconds) {
        this.maximumIntervalSeconds = maximumIntervalSeconds;
    }

    public int getMinimumFailureThreshold() {
        return minimumFailureThreshold;
    }

    public void setMinimumFailureThreshold(int minimumFailureThreshold) {
        this.minimumFailureThreshold = minimumFailureThreshold;
    }

    public int getDefaultFailureThreshold() {
        return defaultFailureThreshold;
    }

    public void setDefaultFailureThreshold(int defaultFailureThreshold) {
        this.defaultFailureThreshold = defaultFailureThreshold;
    }

    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }

    public void setRequestTimeoutSeconds(int requestTimeoutSeconds) {
        this.requestTimeoutSeconds = requestTimeoutSeconds;
    }

    public int getSlowResponseThresholdMillis() {
        return slowResponseThresholdMillis;
    }

    public void setSlowResponseThresholdMillis(int slowResponseThresholdMillis) {
        this.slowResponseThresholdMillis = slowResponseThresholdMillis;
    }

    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }

    public long getSchedulerFixedDelayMillis() {
        return schedulerFixedDelayMillis;
    }

    public void setSchedulerFixedDelayMillis(long schedulerFixedDelayMillis) {
        this.schedulerFixedDelayMillis = schedulerFixedDelayMillis;
    }

    public int getSchedulerBatchSize() {
        return schedulerBatchSize;
    }

    public void setSchedulerBatchSize(int schedulerBatchSize) {
        this.schedulerBatchSize = schedulerBatchSize;
    }
}
