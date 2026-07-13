package com.argus.incidentintelligence.event;

import com.argus.incidentintelligence.service.IncidentIntelligenceEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class IncidentIntelligenceEventListener {

    private static final Logger log = LoggerFactory.getLogger(IncidentIntelligenceEventListener.class);

    private final IncidentIntelligenceEngine incidentIntelligenceEngine;

    public IncidentIntelligenceEventListener(IncidentIntelligenceEngine incidentIntelligenceEngine) {
        this.incidentIntelligenceEngine = incidentIntelligenceEngine;
    }

    @Async
    @EventListener
    public void onIncidentIntelligenceRequested(IncidentIntelligenceRequestedEvent event) {
        try {
            incidentIntelligenceEngine.analyze(event.ownerId(), event.incidentId());
        } catch (RuntimeException exception) {
            log.warn(
                    "Incident intelligence generation failed: ownerId={}, incidentId={}, monitorId={}",
                    event.ownerId(),
                    event.incidentId(),
                    event.monitorId(),
                    exception
            );
        }
    }
}
