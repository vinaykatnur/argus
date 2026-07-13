package com.argus.incidentintelligence.dto;

import com.argus.incidentintelligence.enums.RecommendationCategory;
import com.argus.incidentintelligence.enums.RecommendationPriority;
import java.util.List;

public record RecommendationDto(
        RecommendationPriority priority,
        RecommendationCategory category,
        String recommendation,
        String reason,
        List<String> evidenceReferences,
        Long historicalIncidentId,
        int confidence
) {
    public RecommendationDto {
        evidenceReferences = List.copyOf(evidenceReferences);
    }
}
