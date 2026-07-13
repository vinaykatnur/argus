package com.argus.incidentintelligence.dto;

import com.argus.incidentintelligence.enums.AiProviderName;
import com.argus.incidentintelligence.enums.NarrativeGenerationStatus;
import java.time.Instant;

public record NarrativeSnapshotDto(
        Long id,
        Long ownerId,
        Long incidentId,
        Long analysisSnapshotId,
        int version,
        String executiveSummary,
        String explanation,
        String markdownPostMortem,
        AiProviderName providerName,
        String modelName,
        NarrativeGenerationStatus status,
        String failureReason,
        Long generationDurationMillis,
        Integer tokenUsage,
        Instant generatedAt
) {
}
