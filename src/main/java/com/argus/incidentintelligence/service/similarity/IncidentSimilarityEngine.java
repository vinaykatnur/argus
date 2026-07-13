package com.argus.incidentintelligence.service.similarity;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.argus.incidentintelligence.dto.HistoricalIncidentContext;
import com.argus.incidentintelligence.dto.IncidentContext;
import com.argus.incidentintelligence.dto.SimilarIncidentDto;
import com.argus.incidentintelligence.dto.SimilarityResultDto;
import com.argus.incidentintelligence.enums.SimilarityLevel;
import com.argus.incidentintelligence.util.IncidentFingerprintUtil;

@Service
public class IncidentSimilarityEngine {

    private static final int MIN_MATCH_SCORE = 60;

    public SimilarityResultDto compare(IncidentContext context) {
        List<SimilarIncidentDto> matches = context.historicalIncidents().stream()
                .map(historical -> score(context, historical))
                .filter(match -> match.score() >= MIN_MATCH_SCORE)
                .sorted(Comparator.comparingInt(SimilarIncidentDto::score).reversed())
                .limit(5)
                .toList();

        int bestScore = matches.stream().mapToInt(SimilarIncidentDto::score).max().orElse(0);
        List<String> reasons = new ArrayList<>();
        if (matches.isEmpty()) {
            reasons.add("No historical incident reached the minimum similarity threshold of " + MIN_MATCH_SCORE + "%.");
            reasons.add("The engine rejected the comparison because the available signals were too weak, conflicting, or unrelated.");
        } else {
            reasons.add("Historical comparison combined failure signature, response pattern, escalation, recovery behavior, temporal shape, analytics context, and monitor characteristics.");
            reasons.add("The strongest retained match scored " + bestScore + "% because it satisfied several independent dimensions.");
        }

        return new SimilarityResultDto(bestScore, summary(bestScore), matches, reasons);
    }

    private SimilarIncidentDto score(IncidentContext current, HistoricalIncidentContext historical) {
        List<String> positives = new ArrayList<>();
        List<String> negatives = new ArrayList<>();
        int score = 0;

        score += evaluateFailureSignature(current, historical, positives, negatives);
        score += evaluateResponsePattern(current, historical, positives, negatives);
        score += evaluateEscalationPattern(current, historical, positives, negatives);
        score += evaluateRecoveryPattern(current, historical, positives, negatives);
        score += evaluateTemporalBehavior(current, historical, positives, negatives);
        score += evaluateIncidentShape(current, historical, positives, negatives);
        score += evaluateHistoricalFrequency(current, historical, positives, negatives);
        score += evaluateMonitorCharacteristics(current, historical, positives, negatives);
        score += evaluateTrendShape(current, historical, positives, negatives);

        int cappedScore = Math.max(0, Math.min(100, score));
        if (cappedScore < MIN_MATCH_SCORE) {
            negatives.add("The comparison lacked enough independent signal agreement to justify a meaningful match.");
        }

        return new SimilarIncidentDto(
                historical.incidentId(),
                historical.monitorId(),
                historical.monitorName(),
                historical.startedAt(),
                level(cappedScore),
                cappedScore,
                positives,
                negatives
        );
    }

    private int evaluateFailureSignature(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        String currentFailure = IncidentFingerprintUtil.currentFailureSignature(current);
        String historicalFailure = IncidentFingerprintUtil.failureSignature(
                historical.failureReason(),
                null,
                List.of()
        );
        if (currentFailure.equals(historicalFailure)) {
            positives.add("Failure signature matched: " + currentFailure + ".");
            return 25;
        }
        negatives.add("Failure signature differed: current " + currentFailure + ", historical " + historicalFailure + ".");
        return -12;
    }

    private int evaluateResponsePattern(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        String trend = IncidentFingerprintUtil.trendSignature(current.responseTrend());
        if ("GRADUAL_INCREASE".equals(trend) || "DEGRADATION_TO_FAILURE".equals(trend)) {
            if (historical.downtimeMillis() != null && historical.downtimeMillis() > 0) {
                positives.add("The current trend showed clear degradation before the failure and matched the historical incident shape.");
                return 10;
            }
            positives.add("The current trend showed a clear degradation pattern before the failure.");
            return 6;
        }
        if ("LIMITED_HISTORY".equals(trend) || "LIMITED_HISTORY_WITH_FAILURES".equals(trend)) {
            negatives.add("The current response history was too limited to support a strong response-pattern match.");
            return -8;
        }
        negatives.add("The current response pattern did not show a clear degradation signature.");
        return -4;
    }

    private int evaluateEscalationPattern(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        Integer currentEscalation = current.incident().consecutiveFailedChecks();
        Integer historicalEscalation = historical.consecutiveFailedChecks();
        if (currentEscalation != null && historicalEscalation != null && Math.abs(currentEscalation - historicalEscalation) <= 2) {
            positives.add("Escalation depth was similar across both incidents.");
            return 8;
        }
        if (currentEscalation != null && historicalEscalation != null) {
            negatives.add("Escalation depth diverged materially between the current and historical incident.");
            return -6;
        }
        negatives.add("Escalation depth was incomplete for one of the incidents.");
        return -4;
    }

    private int evaluateRecoveryPattern(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        if (current.incident().resolvedAt() != null && historical.resolved()) {
            positives.add("The current incident had observable recovery context and the historical incident was resolved.");
            return 10;
        }
        if (historical.resolved()) {
            positives.add("The historical incident was resolved, which strengthens its usefulness as a reference.");
            return 4;
        }
        negatives.add("The historical incident was unresolved, limiting its recovery-based reference value.");
        return -8;
    }

    private int evaluateTemporalBehavior(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        Long currentDuration = current.incident().downtimeMillis();
        Long historicalDuration = historical.downtimeMillis();
        if (currentDuration != null && historicalDuration != null && closeDuration(currentDuration, historicalDuration)) {
            positives.add("Incident duration was in a similar range.");
            return 12;
        }
        if (currentDuration == null || historicalDuration == null) {
            negatives.add("Temporal comparison was incomplete because downtime was missing for one incident.");
            return -6;
        }
        negatives.add("Incident duration differed materially.");
        return -8;
    }

    private int evaluateIncidentShape(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        Integer currentEscalation = current.incident().consecutiveFailedChecks();
        Long currentDuration = current.incident().downtimeMillis();
        Integer historicalEscalation = historical.consecutiveFailedChecks();
        if (currentEscalation != null && currentDuration != null && currentEscalation >= 3 && currentDuration > 0) {
            if (historicalEscalation != null && historicalEscalation >= 3) {
                positives.add("The incident shape was consistent with a multi-check degradation pattern.");
                return 8;
            }
            positives.add("The current incident showed a meaningful escalation shape even without a closer historical match.");
            return 4;
        }
        negatives.add("The incident shape did not show a strong degradation pattern.");
        return -4;
    }

    private int evaluateHistoricalFrequency(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        if (current.analyticsAvailable() && current.analytics().incidentCount() > 0) {
            positives.add("Analytics showed recent incident frequency that supported the comparison.");
            return 6;
        }
        negatives.add("Analytics context was unavailable or did not show recent incident frequency.");
        return -6;
    }

    private int evaluateMonitorCharacteristics(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        if (Objects.equals(current.monitor().protocol(), historical.protocol()) && Objects.equals(current.monitor().monitorId(), historical.monitorId())) {
            positives.add("The monitor identity and protocol matched exactly.");
            return 10;
        }
        if (Objects.equals(current.monitor().protocol(), historical.protocol())) {
            positives.add("The monitor protocol matched, which improved the comparison quality.");
            return 6;
        }
        negatives.add("The monitor protocol or identity did not align with the historical incident.");
        return -8;
    }

    private int evaluateTrendShape(
            IncidentContext current,
            HistoricalIncidentContext historical,
            List<String> positives,
            List<String> negatives
    ) {
        String trend = IncidentFingerprintUtil.trendSignature(current.responseTrend());
        if ("GRADUAL_INCREASE".equals(trend) || "DEGRADATION_TO_FAILURE".equals(trend)) {
            positives.add("The trend shape indicated a progressive degradation path.");
            return 8;
        }
        if ("LIMITED_HISTORY".equals(trend) || "LIMITED_HISTORY_WITH_FAILURES".equals(trend)) {
            negatives.add("The trend shape was too limited to support a strong historical comparison.");
            return -6;
        }
        negatives.add("The trend shape did not suggest a clear degradation path.");
        return -4;
    }

    private boolean closeDuration(long currentDuration, long historicalDuration) {
        long difference = Math.abs(currentDuration - historicalDuration);
        long tolerance = Math.max(Duration.ofMinutes(5).toMillis(), Math.max(currentDuration, historicalDuration) / 3);
        return difference <= tolerance;
    }

    private SimilarityLevel level(int score) {
        if (score >= 85) {
            return SimilarityLevel.HIGHLY_SIMILAR;
        }
        if (score >= 70) {
            return SimilarityLevel.MODERATELY_SIMILAR;
        }
        if (score >= 60) {
            return SimilarityLevel.WEAK_SIMILARITY;
        }
        return SimilarityLevel.NO_MEANINGFUL_MATCH;
    }

    private String summary(int score) {
        if (score >= 85) {
            return "Historical incident is highly similar.";
        }
        if (score >= 70) {
            return "Historical incident is moderately similar.";
        }
        if (score >= 60) {
            return "Historical incident has weak similarity.";
        }
        return "No meaningful historical match found.";
    }
}
