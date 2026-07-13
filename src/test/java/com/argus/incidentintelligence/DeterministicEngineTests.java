package com.argus.incidentintelligence;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import com.argus.enums.IncidentStatus;
import com.argus.enums.MonitorStatus;
import com.argus.incidentintelligence.dto.AnalyticsContext;
import com.argus.incidentintelligence.dto.ConfidenceResultDto;
import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.HistoricalIncidentContext;
import com.argus.incidentintelligence.dto.HistoryPoint;
import com.argus.incidentintelligence.dto.IncidentContext;
import com.argus.incidentintelligence.dto.IncidentFacts;
import com.argus.incidentintelligence.dto.MonitorContext;
import com.argus.incidentintelligence.dto.RecommendationDto;
import com.argus.incidentintelligence.dto.SimilarityResultDto;
import com.argus.incidentintelligence.dto.TimelineEventDto;
import com.argus.incidentintelligence.enums.ConfidenceLevel;
import com.argus.incidentintelligence.enums.RecommendationCategory;
import com.argus.incidentintelligence.enums.TimelineEventType;
import com.argus.incidentintelligence.service.confidence.ConfidenceEngine;
import com.argus.incidentintelligence.service.evidence.EvidenceEngine;
import com.argus.incidentintelligence.service.recommendation.RecommendationEngine;
import com.argus.incidentintelligence.service.similarity.IncidentSimilarityEngine;
import com.argus.incidentintelligence.service.timeline.TimelineEngine;

class DeterministicEngineTests {

    private final EvidenceEngine evidenceEngine = new EvidenceEngine();
    private final IncidentSimilarityEngine similarityEngine = new IncidentSimilarityEngine();
    private final ConfidenceEngine confidenceEngine = new ConfidenceEngine();
    private final RecommendationEngine recommendationEngine = new RecommendationEngine();
    private final TimelineEngine timelineEngine = new TimelineEngine();

    @Test
    void evidenceExtractionRecordsFactsWithoutRootCauseClaims() {
        List<EvidenceItemDto> evidence = evidenceEngine.extract(context());

        assertThat(evidence).extracting(EvidenceItemDto::id)
                .contains("incident-opened", "first-failed-check", "response-time-increase");
        assertThat(evidence).noneMatch(item -> item.observation().contains("Database crashed"));
        assertThat(evidence).noneMatch(item -> item.observation().contains("Cloudflare failed"));
    }

    @Test
    void similarityUsesMultipleSignalsAndDoesNotForceLowMatches() {
        SimilarityResultDto similarity = similarityEngine.compare(context());

        assertThat(similarity.bestScore()).isGreaterThanOrEqualTo(50);
        assertThat(similarity.matches()).hasSize(1);
        assertThat(similarity.matches().getFirst().positiveMatches()).isNotEmpty();
        assertThat(similarity.matches().getFirst().negativeDifferences()).isNotEmpty();
    }

    @Test
    void confidenceIsEvidenceBasedAndCapped() {
        SimilarityResultDto similarity = similarityEngine.compare(context());
        ConfidenceResultDto confidence = confidenceEngine.calculate(evidenceEngine.extract(context()), similarity);

        assertThat(confidence.score()).isBetween(1, 95);
        assertThat(confidence.level()).isNotEqualTo(ConfidenceLevel.INSUFFICIENT_EVIDENCE);
        assertThat(confidence.reasons()).anyMatch(reason -> reason.contains("capped below certainty"));
    }

    @Test
    void weakHistoricalMatchesAreRejected() {
        SimilarityResultDto similarity = similarityEngine.compare(weakContext());

        assertThat(similarity.bestScore()).isLessThan(60);
        assertThat(similarity.matches()).isEmpty();
        assertThat(similarity.reasons()).anyMatch(reason -> reason.contains("No historical incident"));
    }

    @Test
    void recommendationsAreInvestigationFocused() {
        SimilarityResultDto similarity = similarityEngine.compare(context());
        List<EvidenceItemDto> evidence = evidenceEngine.extract(context());
        ConfidenceResultDto confidence = confidenceEngine.calculate(evidence, similarity);
        List<RecommendationDto> recommendations = recommendationEngine.generate(evidence, similarity, confidence);

        assertThat(recommendations).isNotEmpty();
        assertThat(recommendations).extracting(RecommendationDto::category)
                .contains(RecommendationCategory.IMMEDIATE_INVESTIGATION);
        assertThat(recommendations).noneMatch(item -> item.recommendation().toLowerCase().contains("restart"));
    }

    @Test
    void timelineSummarizesMeaningfulTransitions() {
        List<TimelineEventDto> timeline = timelineEngine.build(context(), evidenceEngine.extract(context()));

        assertThat(timeline).extracting(TimelineEventDto::eventType)
                .contains(
                        TimelineEventType.PERFORMANCE_DEGRADATION,
                        TimelineEventType.FIRST_FAILED_CHECK,
                        TimelineEventType.INCIDENT_OPENED
                );
    }

    private IncidentContext context() {
        Instant startedAt = Instant.parse("2026-07-03T08:00:00Z");
        return new IncidentContext(
                new MonitorContext(
                        20L,
                        10L,
                        "Checkout API",
                        "https://example.com/health",
                        "HTTPS",
                        60,
                        3,
                        MonitorStatus.DOWN,
                        3,
                        startedAt.plusSeconds(120),
                        startedAt.minusSeconds(60),
                        9000L
                ),
                new IncidentFacts(
                        30L,
                        IncidentStatus.ACTIVE,
                        startedAt,
                        null,
                        null,
                        "HTTP 500",
                        3,
                        startedAt.minusSeconds(60)
                ),
                new AnalyticsContext(
                        true,
                        LocalDate.parse("2026-06-03"),
                        LocalDate.parse("2026-07-03"),
                        1000,
                        990,
                        99.0,
                        300,
                        2,
                        120000,
                        60000,
                        500000
                ),
                List.of(
                        new HistoryPoint(startedAt.minusSeconds(180), true, 200, 200L, null),
                        new HistoryPoint(startedAt.minusSeconds(120), true, 200, 600L, null),
                        new HistoryPoint(startedAt.minusSeconds(60), true, 200, 950L, null),
                        new HistoryPoint(startedAt, false, 500, null, "HTTP 500"),
                        new HistoryPoint(startedAt.plusSeconds(60), false, 500, null, "HTTP 500")
                ),
                List.of(new HistoricalIncidentContext(
                        25L,
                        20L,
                        "Checkout API",
                        "https://example.com/health",
                        "HTTPS",
                        IncidentStatus.RESOLVED,
                        startedAt.minusSeconds(86400),
                        startedAt.minusSeconds(85800),
                        600000L,
                        "HTTP 500",
                        3,
                        true
                )),
                List.of("HTTP 500"),
                startedAt,
                true,
                true
        );
    }

    private IncidentContext weakContext() {
        Instant startedAt = Instant.parse("2026-07-03T08:00:00Z");
        return new IncidentContext(
                new MonitorContext(
                        21L,
                        11L,
                        "Billing API",
                        "https://example.org/health",
                        "HTTPS",
                        60,
                        3,
                        MonitorStatus.DOWN,
                        1,
                        startedAt.plusSeconds(120),
                        startedAt.minusSeconds(60),
                        4000L
                ),
                new IncidentFacts(
                        31L,
                        IncidentStatus.ACTIVE,
                        startedAt,
                        null,
                        null,
                        "SSL certificate expired",
                        1,
                        startedAt.minusSeconds(60)
                ),
                new AnalyticsContext(
                        true,
                        LocalDate.parse("2026-06-03"),
                        LocalDate.parse("2026-07-03"),
                        120,
                        119,
                        99.0,
                        220,
                        0,
                        0,
                        0,
                        0
                ),
                List.of(
                        new HistoryPoint(startedAt.minusSeconds(180), true, 200, 180L, null),
                        new HistoryPoint(startedAt.minusSeconds(120), true, 200, 220L, null),
                        new HistoryPoint(startedAt, false, 502, null, "SSL certificate expired")
                ),
                List.of(new HistoricalIncidentContext(
                        26L,
                        99L,
                        "Search API",
                        "https://example.net/health",
                        "HTTPS",
                        IncidentStatus.RESOLVED,
                        startedAt.minusSeconds(172800),
                        startedAt.minusSeconds(171000),
                        1800000L,
                        "HTTP 500",
                        3,
                        true
                )),
                List.of("SSL certificate expired"),
                startedAt,
                true,
                true
        );
    }
}
