package com.argus.service;

import com.argus.entity.Notification;
import com.argus.entity.NotificationDelivery;
import com.argus.entity.NotificationPreference;
import com.argus.entity.Monitor;
import com.argus.enums.NotificationAuditAction;
import com.argus.enums.NotificationChannel;
import com.argus.enums.NotificationStatus;
import com.argus.enums.NotificationType;
import com.argus.repository.IncidentRepository;
import com.argus.repository.NotificationAuditRepository;
import com.argus.repository.NotificationDeliveryRepository;
import com.argus.repository.NotificationRepository;
import com.argus.repository.NotificationPreferenceRepository;
import com.argus.repository.MonitorRepository;
import com.argus.config.NotificationProperties;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeliveryService {

    private final NotificationRepository notificationRepository;
    private final NotificationDeliveryRepository notificationDeliveryRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final NotificationAuditRepository notificationAuditRepository;
    private final NotificationTemplateService notificationTemplateService;
    private final NotificationChannelResolver notificationChannelResolver;
    private final Map<NotificationChannel, NotificationChannelDeliveryService> deliveryServices;
    private final NotificationAuditService notificationAuditService;
    private final NotificationQueueService notificationQueueService;
    private final NotificationProperties notificationProperties;
    private final MonitorRepository monitorRepository;
    private final IncidentRepository incidentRepository;

    public NotificationDeliveryService(
            NotificationRepository notificationRepository,
            NotificationDeliveryRepository notificationDeliveryRepository,
            NotificationPreferenceRepository notificationPreferenceRepository,
            NotificationAuditRepository notificationAuditRepository,
            NotificationTemplateService notificationTemplateService,
            NotificationChannelResolver notificationChannelResolver,
            List<NotificationChannelDeliveryService> deliveryServices,
            NotificationAuditService notificationAuditService,
            NotificationQueueService notificationQueueService,
            NotificationProperties notificationProperties,
            MonitorRepository monitorRepository,
            IncidentRepository incidentRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationDeliveryRepository = notificationDeliveryRepository;
        this.notificationPreferenceRepository = notificationPreferenceRepository;
        this.notificationAuditRepository = notificationAuditRepository;
        this.notificationTemplateService = notificationTemplateService;
        this.notificationChannelResolver = notificationChannelResolver;
        this.deliveryServices = new ConcurrentHashMap<>();
        deliveryServices.forEach(service -> this.deliveryServices.put(service.channel(), service));
        this.notificationAuditService = notificationAuditService;
        this.notificationQueueService = notificationQueueService;
        this.notificationProperties = notificationProperties;
        this.monitorRepository = monitorRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public void processIncidentEvent(IncidentNotificationEvent event) {
        Monitor monitor = monitorRepository.findById(event.monitorId()).orElse(null);
        if (monitor == null) {
            return;
        }

        NotificationPreference preference = notificationPreferenceRepository.findByMonitor(monitor)
                .orElseGet(() -> notificationPreferenceRepository.save(new NotificationPreference(monitor)));

        var incident = event.incidentId() == null ? null : incidentRepository.findById(event.incidentId()).orElse(null);
        Notification notification = createNotification(event, monitor, incident);
        Notification savedNotification = notificationRepository.save(notification);
        notificationAuditService.record(savedNotification, null, NotificationAuditAction.CREATED, "Notification created");

        Set<NotificationChannel> channels = notificationChannelResolver.resolve(preference, event.type());
        if (channels.isEmpty()) {
            return;
        }
        for (NotificationChannel channel : channels) {
            NotificationDelivery delivery = createDelivery(savedNotification, channel, monitor);
            NotificationDelivery savedDelivery = notificationDeliveryRepository.save(delivery);
            notificationAuditService.record(savedNotification, savedDelivery, NotificationAuditAction.QUEUED, "Delivery queued");
            notificationQueueService.enqueue(savedDelivery.getId());
        }
    }

    @Transactional
    public void processDelivery(Long deliveryId) {
        NotificationDelivery delivery = notificationDeliveryRepository.findById(deliveryId).orElse(null);
        if (delivery == null || delivery.getStatus() == NotificationStatus.SENT) {
            return;
        }

        delivery.setStatus(NotificationStatus.SENDING);
        delivery.setSendingAt(Instant.now());
        notificationDeliveryRepository.save(delivery);
        notificationAuditService.record(delivery.getNotification(), delivery, NotificationAuditAction.SENDING, "Delivery sending");

        try {
            NotificationContent content = notificationTemplateService.render(delivery.getNotification());
            NotificationChannelDeliveryService sender = deliveryServices.get(delivery.getChannel());
            if (sender != null) {
                sender.send(delivery, content);
            }
            delivery.setStatus(NotificationStatus.SENT);
            delivery.setSentAt(Instant.now());
            delivery.setFailureReason(null);
            notificationDeliveryRepository.save(delivery);
            notificationAuditService.record(delivery.getNotification(), delivery, NotificationAuditAction.SENT, "Delivery sent");
        } catch (Exception exception) {
            handleDeliveryFailure(delivery, exception);
        }
    }

    @Transactional
    public void retryPendingDeliveries() {
        List<NotificationDelivery> dueDeliveries = notificationDeliveryRepository.findByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
                NotificationStatus.RETRY_PENDING,
                Instant.now()
        );
        for (NotificationDelivery delivery : dueDeliveries) {
            delivery.setStatus(NotificationStatus.PENDING);
            delivery.setNextRetryAt(null);
            notificationDeliveryRepository.save(delivery);
            notificationQueueService.enqueue(delivery.getId());
        }
    }

    @Transactional
    public void requeuePendingDeliveries() {
        List<NotificationDelivery> pendingDeliveries = notificationDeliveryRepository.findByStatusOrderByCreatedAtAsc(NotificationStatus.PENDING);
        for (NotificationDelivery delivery : pendingDeliveries) {
            notificationQueueService.enqueue(delivery.getId());
        }
    }

    @Transactional(readOnly = true)
    public Page<NotificationDelivery> history(Long ownerId, Long monitorId, Pageable pageable) {
        return notificationDeliveryRepository.findHistory(ownerId, monitorId, pageable);
    }

    @Async
    public void scheduleReminderChecks() {
        List<Notification> notifications = notificationRepository.findAll();
        for (Notification notification : notifications) {
            if (notification.getType() == NotificationType.WEBSITE_DOWN
                    && notification.getIncident() != null
                    && notification.getMonitor() != null) {
                notificationAuditService.record(notification, null, NotificationAuditAction.CREATED, "Reminder scan processed");
            }
        }
    }

    private Notification createNotification(IncidentNotificationEvent event, Monitor monitor, com.argus.entity.Incident incident) {
        Notification notification = new Notification(
                monitor,
                incident,
                event.type(),
                event.eventTime(),
                event.failureReason(),
                event.responseTimeMillis()
        );
        return notification;
    }

    private NotificationDelivery createDelivery(Notification notification, NotificationChannel channel, Monitor monitor) {
        String recipient = monitor.getOwner().getEmail();
        return new NotificationDelivery(notification, channel, recipient, notificationTemplateService.render(notification).subject());
    }

    private void handleDeliveryFailure(NotificationDelivery delivery, Exception exception) {
        delivery.setStatus(NotificationStatus.FAILED);
        delivery.setFailureReason(exception.getMessage());
        delivery.setAttemptCount(delivery.getAttemptCount() + 1);
        notificationDeliveryRepository.save(delivery);
        notificationAuditService.record(delivery.getNotification(), delivery, NotificationAuditAction.FAILED, "Delivery failed");
        notificationQueueService.enqueueRetry(delivery.getId(), nextRetryAt(delivery));
    }

    private Instant nextRetryAt(NotificationDelivery delivery) {
        long seconds = notificationProperties.retryDelaySeconds(delivery.getAttemptCount());
        return Instant.now().plusSeconds(seconds);
    }
}
