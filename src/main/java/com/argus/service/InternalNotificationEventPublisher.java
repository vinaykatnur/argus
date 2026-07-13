package com.argus.service;

import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import com.argus.enums.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class InternalNotificationEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(InternalNotificationEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public InternalNotificationEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishIncidentCreated(Monitor monitor, Incident incident) {
        publish(new IncidentNotificationEvent(
                NotificationType.WEBSITE_DOWN,
                monitor.getId(),
                incident.getId(),
                incident.getStartedAt(),
                incident.getFailureReason(),
                null
        ));
    }

    public void publishIncidentRecovered(Monitor monitor, Incident incident) {
        publish(new IncidentNotificationEvent(
                NotificationType.WEBSITE_RECOVERED,
                monitor.getId(),
                incident.getId(),
                incident.getResolvedAt(),
                incident.getFailureReason(),
                null
        ));
    }

    public void publishSlowResponse(Monitor monitor, HealthEvaluation evaluation, HealthCheckResult result) {
        publish(new IncidentNotificationEvent(
                NotificationType.SLOW_RESPONSE,
                monitor.getId(),
                null,
                monitor.getLastCheckedAt(),
                evaluation.alertMessage(),
                result.responseTimeMillis()
        ));
    }

    public void publishReminder(Monitor monitor, Incident incident) {
        publish(new IncidentNotificationEvent(
                NotificationType.WEBSITE_DOWN_REMINDER,
                monitor.getId(),
                incident.getId(),
                java.time.Instant.now(),
                incident.getFailureReason(),
                null
        ));
    }

    private void publish(IncidentNotificationEvent event) {
        try {
            eventPublisher.publishEvent(event);
        } catch (RuntimeException exception) {
            log.warn(
                    "Notification event publication failed: monitorId={}, incidentId={}, type={}",
                    event.monitorId(),
                    event.incidentId(),
                    event.type(),
                    exception
            );
        }
    }
}
