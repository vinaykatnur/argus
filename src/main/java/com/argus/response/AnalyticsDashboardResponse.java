package com.argus.response;

import java.util.List;

public record AnalyticsDashboardResponse(
        DashboardSummaryResponse summary,
        List<MonitorListItemResponse> pinnedMonitors,
        List<MonitorListItemResponse> needsAttention,
        List<AnalyticsTrendPointResponse> availabilityTrend,
        List<AnalyticsTrendPointResponse> responseTimeTrend,
        List<AnalyticsTrendPointResponse> incidentTrend
) {
}
