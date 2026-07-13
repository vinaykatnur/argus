package com.argus.config;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Validated
@Component
@ConfigurationProperties(prefix = "argus.notifications")
public class NotificationProperties {

    @Min(1)
    @Max(50)
    private int workerPoolSize = 4;

    @Min(1)
    private int retryWorkerPoolSize = 1;

    @Min(1)
    private int maxRetryAttempts = 3;

    @Min(1)
    private long retryInitialDelaySeconds = 30;

    @Min(1)
    private int retryBackoffMultiplier = 4;

    @Min(60)
    private long reminderScanFixedDelayMillis = 60000;

    public int getWorkerPoolSize() {
        return workerPoolSize;
    }

    public void setWorkerPoolSize(int workerPoolSize) {
        this.workerPoolSize = workerPoolSize;
    }

    public int getRetryWorkerPoolSize() {
        return retryWorkerPoolSize;
    }

    public void setRetryWorkerPoolSize(int retryWorkerPoolSize) {
        this.retryWorkerPoolSize = retryWorkerPoolSize;
    }

    public int getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public void setMaxRetryAttempts(int maxRetryAttempts) {
        this.maxRetryAttempts = maxRetryAttempts;
    }

    public long getRetryInitialDelaySeconds() {
        return retryInitialDelaySeconds;
    }

    public void setRetryInitialDelaySeconds(long retryInitialDelaySeconds) {
        this.retryInitialDelaySeconds = retryInitialDelaySeconds;
    }

    public int getRetryBackoffMultiplier() {
        return retryBackoffMultiplier;
    }

    public void setRetryBackoffMultiplier(int retryBackoffMultiplier) {
        this.retryBackoffMultiplier = retryBackoffMultiplier;
    }

    public long getReminderScanFixedDelayMillis() {
        return reminderScanFixedDelayMillis;
    }

    public void setReminderScanFixedDelayMillis(long reminderScanFixedDelayMillis) {
        this.reminderScanFixedDelayMillis = reminderScanFixedDelayMillis;
    }

    public long retryDelaySeconds(int failedAttemptCount) {
        long delay = retryInitialDelaySeconds;
        for (int i = 1; i < failedAttemptCount; i++) {
            delay *= retryBackoffMultiplier;
        }
        return delay;
    }
}
