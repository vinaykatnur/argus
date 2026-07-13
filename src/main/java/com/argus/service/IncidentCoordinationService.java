package com.argus.service;

import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import com.argus.enums.IncidentStatus;
import com.argus.incidentintelligence.event.IncidentIntelligenceEventPublisher;
import com.argus.repository.IncidentRepository;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IncidentCoordinationService {

    private static final Logger log = LoggerFactory.getLogger(IncidentCoordinationService.class);

    private final IncidentRepository incidentRepository;
    private final InternalNotificationEventPublisher notificationEventPublisher;
    private final IncidentIntelligenceEventPublisher intelligenceEventPublisher;

    public IncidentCoordinationService(
            IncidentRepository incidentRepository,
            InternalNotificationEventPublisher notificationEventPublisher,
            IncidentIntelligenceEventPublisher intelligenceEventPublisher
    ) {
        this.incidentRepository = incidentRepository;
        this.notificationEventPublisher = notificationEventPublisher;
        this.intelligenceEventPublisher = intelligenceEventPublisher;
    }

    public void createIncidentIfAbsent(Monitor monitor, Instant startedAt) {
        incidentRepository.findFirstByMonitorAndStatusOrderByStartedAtDesc(monitor, IncidentStatus.ACTIVE)
                .ifPresentOrElse(
                        incident -> {
                        },
                        () -> createIncident(monitor, startedAt)
                );
    }

    public void resolveActiveIncidentIfPresent(Monitor monitor, Instant resolvedAt) {
        incidentRepository.findFirstByMonitorAndStatusOrderByStartedAtDesc(monitor, IncidentStatus.ACTIVE)
                .ifPresent(incident -> resolveIncident(monitor, incident, resolvedAt));
    }

    private void createIncident(Monitor monitor, Instant startedAt) {
        Incident incident = incidentRepository.save(new Incident(monitor, startedAt));
        log.info("Incident created: monitorId={}, incidentId={}", monitor.getId(), incident.getId());
        notificationEventPublisher.publishIncidentCreated(monitor, incident);
        intelligenceEventPublisher.publishIncidentCreated(monitor, incident);
    }

    private void resolveIncident(Monitor monitor, Incident incident, Instant resolvedAt) {
        incident.resolve(resolvedAt);
        log.info("Incident resolved: monitorId={}, incidentId={}", monitor.getId(), incident.getId());
        notificationEventPublisher.publishIncidentRecovered(monitor, incident);
    }
}
