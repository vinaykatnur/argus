package com.argus.incidentintelligence.service.confidence;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.argus.incidentintelligence.dto.ConfidenceResultDto;
import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.SimilarityResultDto;
import com.argus.incidentintelligence.enums.ConfidenceLevel;
import com.argus.incidentintelligence.enums.EvidenceType;

@Service
public class ConfidenceEngine {

    private static final int MAX_CONFIDENCE = 95;

    public ConfidenceResultDto calculate(List<EvidenceItemDto> evidence, SimilarityResultDto similarity) {
        List<String> positives = new ArrayList<>();
        List<String> negatives = new ArrayList<>();
        int score = 15;

        if (evidence.isEmpty()) {
            negatives.add("No evidence items were available.");
            return result(0, positives, negatives);
        }

        int evidenceWeight = evidence.stream().mapToInt(EvidenceItemDto::weight).sum();
        int evidenceCompleteness = Math.min(20, evidenceWeight / 6);
        score += evidenceCompleteness;
        positives.add("Evidence completeness contributed " + evidenceCompleteness + " confidence point(s).");

        Set<EvidenceType> categories = EnumSet.noneOf(EvidenceType.class);
        evidence.stream().map(EvidenceItemDto::type).forEach(categories::add);
        if (categories.size() >= 4) {
            score += 10;
            positives.add("Evidence spanned " + categories.size() + " categories.");
        } else {
            score -= 6;
            negatives.add("Evidence spanned only " + categories.size() + " category/categories.");
        }

        if (contains(evidence, "positive evidence") || contains(evidence, "positive evidence:")) {
            score += 6;
            positives.add("Observed evidence included explicit positive signals.");
        }

        if (contains(evidence, "negative evidence") || contains(evidence, "negative evidence:")) {
            score -= 8;
            negatives.add("Negative evidence reduced completeness and raised uncertainty.");
        }

        if (similarity.bestScore() >= 80) {
            score += 10;
            positives.add("Historical reinforcement was strong enough to support the conclusion.");
        } else if (similarity.bestScore() >= 60) {
            score += 6;
            positives.add("Historical reinforcement was moderate and partially supported the conclusion.");
        } else {
            score -= 10;
            negatives.add("Historical reinforcement was weak, increasing uncertainty.");
        }

        if (contains(evidence, "analytics summary was unavailable") || contains(evidence, "analytics summary was unavailable")) {
            score -= 12;
            negatives.add("Missing analytics reduced completeness and historical context.");
        }

        if (contains(evidence, "failed check") && contains(evidence, "response time increased")) {
            score += 8;
            positives.add("Performance degradation and failed checks agreed on the same incident pattern.");
        }

        if (hasContradiction(evidence)) {
            score -= 12;
            negatives.add("Conflicting failure signals reduced confidence.");
        }

        if (evidence.size() < 4) {
            score -= 8;
            negatives.add("The evidence set was small, so the conclusion stayed conservative.");
        }

        return result(Math.min(MAX_CONFIDENCE, Math.max(0, score)), positives, negatives);
    }

    private ConfidenceResultDto result(int score, List<String> positives, List<String> negatives) {
        List<String> reasons = new ArrayList<>();
        reasons.add("Confidence is evidence-based and capped below certainty.");
        reasons.addAll(positives);
        reasons.addAll(negatives);
        return new ConfidenceResultDto(score, level(score), reasons, positives, negatives);
    }

    private ConfidenceLevel level(int score) {
        if (score <= 0) {
            return ConfidenceLevel.INSUFFICIENT_EVIDENCE;
        }
        if (score >= 85) {
            return ConfidenceLevel.VERY_HIGH;
        }
        if (score >= 70) {
            return ConfidenceLevel.HIGH;
        }
        if (score >= 50) {
            return ConfidenceLevel.MODERATE;
        }
        if (score >= 30) {
            return ConfidenceLevel.LOW;
        }
        return ConfidenceLevel.VERY_LOW;
    }

    private boolean contains(List<EvidenceItemDto> evidence, String phrase) {
        String needle = phrase.toLowerCase(Locale.ROOT);
        return evidence.stream()
                .map(EvidenceItemDto::observation)
                .anyMatch(observation -> observation.toLowerCase(Locale.ROOT).contains(needle));
    }

    private boolean hasContradiction(List<EvidenceItemDto> evidence) {
        boolean timeout = contains(evidence, "timeout") || contains(evidence, "timed out");
        boolean dns = contains(evidence, "dns") || contains(evidence, "unknown host");
        boolean ssl = contains(evidence, "ssl") || contains(evidence, "certificate");
        boolean http5xx = contains(evidence, "http status 5");
        int signals = 0;
        if (timeout) {
            signals++;
        }
        if (dns) {
            signals++;
        }
        if (ssl) {
            signals++;
        }
        if (http5xx) {
            signals++;
        }
        return signals > 1;
    }
}
