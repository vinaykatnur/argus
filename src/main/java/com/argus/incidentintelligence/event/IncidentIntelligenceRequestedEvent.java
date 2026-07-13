package com.argus.incidentintelligence.event;

import java.time.Instant;

public record IncidentIntelligenceRequestedEvent(
        Long ownerId,
        Long incidentId,
        Long monitorId,
        Instant requestedAt
) {
}
