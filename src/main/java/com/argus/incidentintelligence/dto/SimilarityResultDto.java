package com.argus.incidentintelligence.dto;

import java.util.List;

public record SimilarityResultDto(
        int bestScore,
        String summary,
        List<SimilarIncidentDto> matches,
        List<String> reasons
) {
    public SimilarityResultDto {
        matches = List.copyOf(matches);
        reasons = List.copyOf(reasons);
    }
}
