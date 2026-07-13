package com.argus.service;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventListener {

    private final NotificationDeliveryService notificationDeliveryService;

    public NotificationEventListener(NotificationDeliveryService notificationDeliveryService) {
        this.notificationDeliveryService = notificationDeliveryService;
    }

    @Async
    @EventListener
    public void onIncidentNotification(IncidentNotificationEvent event) {
        notificationDeliveryService.processIncidentEvent(event);
    }
}
