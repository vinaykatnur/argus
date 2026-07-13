package com.argus.response;

import com.argus.enums.MonitorStatus;
import java.time.Instant;
import java.util.List;

public record MonitorDetailsResponse(
        Long id,
        MonitorStatus currentStatus,
        ResponseTimeSummaryResponse responseTimes,
        MonitorConfigurationResponse configuration,
        NotificationPreferencesResponse notificationPreferences,
        boolean paused,
        boolean pinned,
        Integer pinnedPosition,
        Instant lastCheckedAt,
        CurrentIncidentResponse currentIncident,
        List<IncidentTimelineItemResponse> recentIncidents
) {
}
