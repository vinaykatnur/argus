package com.argus.response;

import com.argus.entity.Monitor;
import com.argus.enums.MonitorStatus;
import java.time.Instant;

public record MonitorListItemResponse(
        Long id,
        String url,
        String displayName,
        MonitorStatus status,
        boolean paused,
        boolean pinned,
        Integer pinnedPosition,
        Long currentResponseTimeMillis,
        Instant lastCheckedAt,
        boolean activeIncident,
        long incidentCount
) {

    public static MonitorListItemResponse from(Monitor monitor, boolean activeIncident) {
        return new MonitorListItemResponse(
                monitor.getId(),
                monitor.getUrl(),
                monitor.getDisplayName(),
                monitor.getStatus(),
                monitor.isPaused(),
                monitor.isPinned(),
                monitor.getPinnedPosition(),
                monitor.getLastResponseTimeMillis(),
                monitor.getLastCheckedAt(),
                activeIncident,
                monitor.getIncidentCount()
        );
    }
}
