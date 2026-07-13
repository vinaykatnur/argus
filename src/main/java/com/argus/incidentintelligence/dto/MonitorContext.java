package com.argus.incidentintelligence.dto;

import com.argus.enums.MonitorStatus;
import java.time.Instant;

public record MonitorContext(
        Long monitorId,
        Long ownerId,
        String displayName,
        String url,
        String protocol,
        int intervalSeconds,
        int failureThreshold,
        MonitorStatus status,
        int consecutiveFailureCount,
        Instant lastCheckedAt,
        Instant lastSuccessfulCheckAt,
        Long lastResponseTimeMillis
) {
}
