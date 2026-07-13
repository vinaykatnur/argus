package com.argus.incidentintelligence.dto;

import com.argus.incidentintelligence.enums.EvidenceType;
import java.time.Instant;

public record EvidenceItemDto(
        String id,
        EvidenceType type,
        String observation,
        String source,
        Instant timestamp,
        int weight
) {
}
