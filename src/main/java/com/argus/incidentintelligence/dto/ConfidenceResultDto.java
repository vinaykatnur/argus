package com.argus.incidentintelligence.dto;

import com.argus.incidentintelligence.enums.ConfidenceLevel;
import java.util.List;

public record ConfidenceResultDto(
        int score,
        ConfidenceLevel level,
        List<String> reasons,
        List<String> positiveContributors,
        List<String> negativeContributors
) {
    public ConfidenceResultDto {
        reasons = List.copyOf(reasons);
        positiveContributors = List.copyOf(positiveContributors);
        negativeContributors = List.copyOf(negativeContributors);
    }
}
