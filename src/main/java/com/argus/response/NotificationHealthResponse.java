package com.argus.response;

public record NotificationHealthResponse(
        int notificationQueueSize,
        int retryQueueSize,
        long failedNotificationCount
) {
}
