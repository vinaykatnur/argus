package com.argus.incidentintelligence.dto;

import com.argus.incidentintelligence.enums.SimilarityLevel;
import java.time.Instant;
import java.util.List;

public record SimilarIncidentDto(
        Long incidentId,
        Long monitorId,
        String monitorName,
        Instant startedAt,
        SimilarityLevel level,
        int score,
        List<String> positiveMatches,
        List<String> negativeDifferences
) {
    public SimilarIncidentDto {
        positiveMatches = List.copyOf(positiveMatches);
        negativeDifferences = List.copyOf(negativeDifferences);
    }
}
