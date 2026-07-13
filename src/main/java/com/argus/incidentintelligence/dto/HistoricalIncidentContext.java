package com.argus.incidentintelligence.dto;

import com.argus.enums.IncidentStatus;
import java.time.Instant;

public record HistoricalIncidentContext(
        Long incidentId,
        Long monitorId,
        String monitorName,
        String monitorUrl,
        String protocol,
        IncidentStatus status,
        Instant startedAt,
        Instant resolvedAt,
        Long downtimeMillis,
        String failureReason,
        Integer consecutiveFailedChecks,
        boolean resolved
) {
}
