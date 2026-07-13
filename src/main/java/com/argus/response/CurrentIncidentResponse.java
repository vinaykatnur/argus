package com.argus.response;

import java.time.Instant;

public record CurrentIncidentResponse(
        Long incidentId,
        Instant incidentStartTime,
        String failureReason,
        int failureThreshold,
        int consecutiveFailedChecks,
        Instant lastSuccessfulCheck
) {
}
