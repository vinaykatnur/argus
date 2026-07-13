package com.argus.incidentintelligence.dto;

import com.argus.incidentintelligence.enums.TimelineEventType;
import java.time.Instant;
import java.util.List;

public record TimelineEventDto(
        Instant timestamp,
        TimelineEventType eventType,
        String description,
        List<String> supportingEvidence
) {
    public TimelineEventDto {
        supportingEvidence = List.copyOf(supportingEvidence);
    }
}
