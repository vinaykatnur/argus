package com.argus.service;

import com.argus.enums.NotificationType;
import java.time.Instant;

public record IncidentNotificationEvent(
        NotificationType type,
        Long monitorId,
        Long incidentId,
        Instant eventTime,
        String failureReason,
        Long responseTimeMillis
) {
}
