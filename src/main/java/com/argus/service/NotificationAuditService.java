package com.argus.service;

import com.argus.entity.Notification;
import com.argus.entity.NotificationAudit;
import com.argus.entity.NotificationDelivery;
import com.argus.enums.NotificationAuditAction;
import com.argus.repository.NotificationAuditRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationAuditService {

    private final NotificationAuditRepository notificationAuditRepository;

    public NotificationAuditService(NotificationAuditRepository notificationAuditRepository) {
        this.notificationAuditRepository = notificationAuditRepository;
    }

    public void record(
            Notification notification,
            NotificationDelivery delivery,
            NotificationAuditAction action,
            String message
    ) {
        notificationAuditRepository.save(new NotificationAudit(notification, delivery, action, message));
    }

    public long failedNotificationCount() {
        return notificationAuditRepository.findAll().stream()
                .filter(audit -> audit.getAction() == NotificationAuditAction.FAILED)
                .count();
    }
}
