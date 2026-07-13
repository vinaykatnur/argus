package com.argus.response;

import java.time.LocalDate;

public record AnalyticsSummaryResponse(
        LocalDate date,
        long totalChecks,
        long successfulChecks,
        double availabilityPercent,
        Long averageResponseTimeMillis,
        long incidentCount,
        long totalDowntimeMillis,
        Long averageIncidentDurationMillis,
        Long mtbfMillis
) {
}
