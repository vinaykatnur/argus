package com.argus.response;

import com.argus.entity.Monitor;
import com.argus.enums.MonitorStatus;
import java.time.Instant;

public record MonitorResponse(
        Long id,
        String url,
        String displayName,
        int intervalSeconds,
        int failureThreshold,
        boolean emailDowntimeNotificationsEnabled,
        boolean emailRecoveryNotificationsEnabled,
        boolean active,
        MonitorStatus status,
        int consecutiveFailureCount,
        Instant lastCheckedAt,
        Instant lastSuccessfulCheckAt,
        Long lastResponseTimeMillis,
        Instant nextCheckAt,
        boolean pinned,
        Integer pinnedPosition,
        Instant createdAt,
        Instant updatedAt
) {

    public static MonitorResponse from(Monitor monitor) {
        return new MonitorResponse(
                monitor.getId(),
                monitor.getUrl(),
                monitor.getDisplayName(),
                monitor.getIntervalSeconds(),
                monitor.getFailureThreshold(),
                monitor.isEmailDowntimeNotificationsEnabled(),
                monitor.isEmailRecoveryNotificationsEnabled(),
                monitor.isActive(),
                monitor.getStatus(),
                monitor.getConsecutiveFailureCount(),
                monitor.getLastCheckedAt(),
                monitor.getLastSuccessfulCheckAt(),
                monitor.getLastResponseTimeMillis(),
                monitor.getNextCheckAt(),
                monitor.isPinned(),
                monitor.getPinnedPosition(),
                monitor.getCreatedAt(),
                monitor.getUpdatedAt()
        );
    }
}
