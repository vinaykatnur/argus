package com.argus.incidentintelligence.dto;

import java.time.Instant;

public record HistoryPoint(
        Instant checkedAt,
        boolean successful,
        Integer httpStatusCode,
        Long responseTimeMillis,
        String failureReason
) {
}
