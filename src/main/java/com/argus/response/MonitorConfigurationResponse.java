package com.argus.response;

public record MonitorConfigurationResponse(
        String url,
        String displayName,
        int monitoringIntervalSeconds,
        int failureThreshold
) {
}
