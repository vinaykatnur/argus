package com.argus.incidentintelligence.dto;

import java.time.LocalDate;

public record AnalyticsContext(
        boolean analyticsAvailable,
        LocalDate startDate,
        LocalDate endDate,
        long totalChecks,
        long successfulChecks,
        double availabilityPercent,
        long averageResponseTimeMillis,
        long incidentCount,
        long totalDowntimeMillis,
        long averageIncidentDurationMillis,
        long mtbfMillis
) {
    public static AnalyticsContext unavailable(LocalDate startDate, LocalDate endDate) {
        return new AnalyticsContext(false, startDate, endDate, 0, 0, 0, 0, 0, 0, 0, 0);
    }
}
