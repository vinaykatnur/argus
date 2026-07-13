package com.argus.incidentintelligence.dto;

import java.time.Instant;
import java.util.List;

public record IncidentAnalysisSnapshotDto(
        Long id,
        Long ownerId,
        Long incidentId,
        Long monitorId,
        int version,
        String engineVersion,
        String incidentSummary,
        String probablePattern,
        List<EvidenceItemDto> evidence,
        SimilarityResultDto similarity,
        ConfidenceResultDto confidence,
        List<RecommendationDto> recommendations,
        List<TimelineEventDto> timeline,
        Instant generatedAt
) {
    public IncidentAnalysisSnapshotDto {
        evidence = List.copyOf(evidence);
        recommendations = List.copyOf(recommendations);
        timeline = List.copyOf(timeline);
    }
}
