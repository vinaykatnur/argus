package com.argus.response;

import java.util.List;

public record MonitorAnalyticsResponse(
        Long monitorId,
        double availabilityPercent,
        Long averageResponseTimeMillis,
        long incidentCount,
        long totalDowntimeMillis,
        Long averageIncidentDurationMillis,
        Long mtbfMillis,
        List<AnalyticsTrendPointResponse> availabilityTrend,
        List<AnalyticsTrendPointResponse> responseTimeTrend,
        List<AnalyticsTrendPointResponse> incidentTrend
) {
}
