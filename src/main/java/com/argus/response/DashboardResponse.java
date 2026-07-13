package com.argus.response;

import java.util.List;

public record DashboardResponse(
        DashboardSummaryResponse summary,
        List<MonitorListItemResponse> pinnedMonitors,
        List<MonitorListItemResponse> needsAttention,
        List<IncidentTimelineItemResponse> recentIncidents,
        DashboardMessagesResponse messages
) {
}
