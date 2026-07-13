package com.argus.incidentintelligence.dto;

import com.argus.enums.IncidentStatus;
import java.time.Instant;

public record IncidentFacts(
        Long incidentId,
        IncidentStatus status,
        Instant startedAt,
        Instant resolvedAt,
        Long downtimeMillis,
        String failureReason,
        Integer consecutiveFailedChecks,
        Instant lastSuccessfulCheckAt
) {
}
