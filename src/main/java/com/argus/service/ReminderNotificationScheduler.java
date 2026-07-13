package com.argus.service;

import com.argus.config.NotificationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReminderNotificationScheduler {

    private final NotificationDeliveryService notificationDeliveryService;
    private final NotificationProperties notificationProperties;

    public ReminderNotificationScheduler(
            NotificationDeliveryService notificationDeliveryService,
            NotificationProperties notificationProperties
    ) {
        this.notificationDeliveryService = notificationDeliveryService;
        this.notificationProperties = notificationProperties;
    }

    @Scheduled(fixedDelayString = "${argus.notifications.reminder-scan-fixed-delay-millis:60000}")
    public void scanReminders() {
        notificationDeliveryService.scheduleReminderChecks();
    }
}
