package com.argus.response;

import com.argus.entity.NotificationDelivery;
import com.argus.enums.NotificationChannel;
import com.argus.enums.NotificationStatus;
import com.argus.enums.NotificationType;
import java.time.Instant;

public record NotificationHistoryResponse(
        Long notificationId,
        Long deliveryId,
        Long monitorId,
        String monitorName,
        NotificationType notificationType,
        NotificationChannel channel,
        NotificationStatus status,
        Instant createdTime,
        Instant sentTime,
        String failureReason
) {

    public static NotificationHistoryResponse from(NotificationDelivery delivery) {
        return new NotificationHistoryResponse(
                delivery.getNotification().getId(),
                delivery.getId(),
                delivery.getNotification().getMonitor().getId(),
                delivery.getNotification().getMonitor().getDisplayName(),
                delivery.getNotification().getType(),
                delivery.getChannel(),
                delivery.getStatus(),
                delivery.getCreatedAt(),
                delivery.getSentAt(),
                delivery.getFailureReason()
        );
    }
}
