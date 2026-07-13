package com.argus.incidentintelligence.service.recommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.argus.incidentintelligence.dto.ConfidenceResultDto;
import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.RecommendationDto;
import com.argus.incidentintelligence.dto.SimilarityResultDto;
import com.argus.incidentintelligence.enums.RecommendationCategory;
import com.argus.incidentintelligence.enums.RecommendationPriority;

@Service
public class RecommendationEngine {

    public List<RecommendationDto> generate(
            List<EvidenceItemDto> evidence,
            SimilarityResultDto similarity,
            ConfidenceResultDto confidence
    ) {
        List<RecommendationDto> recommendations = new ArrayList<>();

        evidence.stream()
                .filter(item -> contains(item, "response time increased"))
                .findFirst()
                .ifPresent(item -> recommendations.add(new RecommendationDto(
                        RecommendationPriority.HIGH,
                        RecommendationCategory.IMMEDIATE_INVESTIGATION,
                        "Step 1: inspect application logs and latency around the incident window before changing any infrastructure.",
                        "Performance degradation was observed and should be verified against the relevant application logs.",
                        List.of(item.id()),
                        null,
                        Math.max(confidence.score() - 5, 20)
                )));

        evidence.stream()
                .filter(item -> contains(item, "failed check") || contains(item, "failure threshold"))
                .findFirst()
                .ifPresent(item -> recommendations.add(new RecommendationDto(
                        RecommendationPriority.HIGH,
                        RecommendationCategory.IMMEDIATE_INVESTIGATION,
                        "Step 2: inspect the monitor history and the endpoint that produced the failed check to confirm the observed failure signature.",
                        "The evidence shows a concrete failed check and should be confirmed against the monitor history before further action.",
                        List.of(item.id()),
                        null,
                        Math.max(confidence.score() - 3, 25)
                )));

        similarity.matches().stream()
                .findFirst()
                .ifPresent(match -> recommendations.add(new RecommendationDto(
                        RecommendationPriority.MEDIUM,
                        RecommendationCategory.HISTORICAL_REFERENCE,
                        "Step 3: compare this incident with historical incident " + match.incidentId() + " to validate the deterministic similarity reasoning.",
                        "The historical comparison produced a traceable match and should be reviewed as part of the investigation workflow.",
                        List.of(),
                        match.incidentId(),
                        Math.min(confidence.score(), match.score())
                )));

        evidence.stream()
                .filter(item -> contains(item, "analytics summary was unavailable"))
                .findFirst()
                .ifPresent(item -> recommendations.add(new RecommendationDto(
                        RecommendationPriority.MEDIUM,
                        RecommendationCategory.MISSING_EVIDENCE,
                        "Step 4: review analytics summaries and monitor history once they are available to improve confidence.",
                        "Missing analytics reduce completeness and weaken the historical context.",
                        List.of(item.id()),
                        null,
                        Math.max(0, confidence.score() - 15)
                )));

        if (recommendations.isEmpty()) {
            recommendations.add(new RecommendationDto(
                    RecommendationPriority.MEDIUM,
                    RecommendationCategory.MISSING_EVIDENCE,
                    "Step 1: collect additional monitoring history before drawing a conclusion.",
                    "Current evidence is insufficient for a specific investigation path.",
                    evidence.stream().map(EvidenceItemDto::id).limit(3).toList(),
                    null,
                    confidence.score()
            ));
        }

        return recommendations.stream()
                .sorted(Comparator.comparing(RecommendationDto::priority)
                        .thenComparingInt(RecommendationDto::confidence).reversed())
                .limit(6)
                .toList();
    }

    private boolean contains(EvidenceItemDto item, String phrase) {
        return item.observation().toLowerCase(Locale.ROOT).contains(phrase.toLowerCase(Locale.ROOT));
    }
}
