package com.argus.response;

public record DashboardSummaryResponse(
        long totalMonitors,
        long healthy,
        long down,
        long slow,
        long paused,
        long activeIncidents,
        Long averageResponseTimeMillis
) {
}
