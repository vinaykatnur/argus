package com.argus.incidentintelligence.event;

import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class IncidentIntelligenceEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(IncidentIntelligenceEventPublisher.class);

    private final ApplicationEventPublisher eventPublisher;

    public IncidentIntelligenceEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publishIncidentCreated(Monitor monitor, Incident incident) {
        try {
            eventPublisher.publishEvent(new IncidentIntelligenceRequestedEvent(
                    monitor.getOwner().getId(),
                    incident.getId(),
                    monitor.getId(),
                    incident.getStartedAt()
            ));
        } catch (RuntimeException exception) {
            log.warn(
                    "Incident intelligence event publication failed: monitorId={}, incidentId={}",
                    monitor.getId(),
                    incident.getId(),
                    exception
            );
        }
    }
}
