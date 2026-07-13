package com.argus.incidentintelligence.service;

import com.argus.incidentintelligence.dto.ConfidenceResultDto;
import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.IncidentAnalysisSnapshotDto;
import com.argus.incidentintelligence.dto.IncidentContext;
import com.argus.incidentintelligence.dto.RecommendationDto;
import com.argus.incidentintelligence.dto.SimilarityResultDto;
import com.argus.incidentintelligence.dto.TimelineEventDto;
import com.argus.incidentintelligence.service.confidence.ConfidenceEngine;
import com.argus.incidentintelligence.service.context.IncidentContextBuilder;
import com.argus.incidentintelligence.service.evidence.EvidenceEngine;
import com.argus.incidentintelligence.service.recommendation.RecommendationEngine;
import com.argus.incidentintelligence.service.similarity.IncidentSimilarityEngine;
import com.argus.incidentintelligence.service.snapshot.IncidentAnalysisSnapshotService;
import com.argus.incidentintelligence.service.timeline.TimelineEngine;
import com.argus.incidentintelligence.util.IncidentFingerprintUtil;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IncidentIntelligenceEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(IncidentIntelligenceEngine.class);

    private final IncidentContextBuilder contextBuilder;
    private final IncidentSimilarityEngine similarityEngine;
    private final EvidenceEngine evidenceEngine;
    private final ConfidenceEngine confidenceEngine;
    private final RecommendationEngine recommendationEngine;
    private final TimelineEngine timelineEngine;
    private final IncidentAnalysisSnapshotService snapshotService;

    public IncidentIntelligenceEngine(
            IncidentContextBuilder contextBuilder,
            IncidentSimilarityEngine similarityEngine,
            EvidenceEngine evidenceEngine,
            ConfidenceEngine confidenceEngine,
            RecommendationEngine recommendationEngine,
            TimelineEngine timelineEngine,
            IncidentAnalysisSnapshotService snapshotService
    ) {
        this.contextBuilder = contextBuilder;
        this.similarityEngine = similarityEngine;
        this.evidenceEngine = evidenceEngine;
        this.confidenceEngine = confidenceEngine;
        this.recommendationEngine = recommendationEngine;
        this.timelineEngine = timelineEngine;
        this.snapshotService = snapshotService;
    }

    public IncidentAnalysisSnapshotDto analyze(Long ownerId, Long incidentId) {
        long startedAt = System.nanoTime();
        IncidentContext context = contextBuilder.build(ownerId, incidentId);
        long contextTime = elapsedMillis(startedAt);

        SimilarityResultDto similarity = similarityEngine.compare(context);
        long similarityTime = elapsedMillis(startedAt);

        List<EvidenceItemDto> evidence = evidenceEngine.extract(context);
        long evidenceTime = elapsedMillis(startedAt);

        ConfidenceResultDto confidence = confidenceEngine.calculate(evidence, similarity);
        long confidenceTime = elapsedMillis(startedAt);

        List<RecommendationDto> recommendations = recommendationEngine.generate(evidence, similarity, confidence);
        long recommendationTime = elapsedMillis(startedAt);

        List<TimelineEventDto> timeline = timelineEngine.build(context, evidence);
        long timelineTime = elapsedMillis(startedAt);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(
                    "Incident intelligence timings: context={}ms similarity={}ms evidence={}ms confidence={}ms recommendations={}ms timeline={}ms total={}ms",
                    contextTime,
                    similarityTime - contextTime,
                    evidenceTime - similarityTime,
                    confidenceTime - evidenceTime,
                    recommendationTime - confidenceTime,
                    timelineTime - recommendationTime,
                    timelineTime
            );
        }

        return snapshotService.persist(
                ownerId,
                context,
                probablePattern(context, confidence),
                evidence,
                similarity,
                confidence,
                recommendations,
                timeline
        );
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000;
    }

    private String probablePattern(IncidentContext context, ConfidenceResultDto confidence) {
        if (confidence.score() < 30) {
            return "Unable to determine probable cause with current evidence.";
        }

        String signature = IncidentFingerprintUtil.currentFailureSignature(context);
        return switch (signature) {
            case "HTTP_5XX" -> "Pattern suggests monitored endpoint server-side error responses.";
            case "HTTP_4XX" -> "Pattern suggests monitored endpoint client-visible error responses.";
            case "TIMEOUT" -> "Pattern suggests monitored endpoint timeout behavior.";
            case "SSL_FAILURE" -> "Pattern suggests TLS or certificate validation failure.";
            case "DNS_FAILURE" -> "Pattern suggests name resolution failure for the monitored endpoint.";
            case "CONNECTION_REFUSED" -> "Pattern suggests refused connection to the monitored endpoint.";
            case "UNKNOWN_FAILURE" -> "Unable to determine probable cause with current evidence.";
            default -> "Pattern suggests monitored endpoint degradation.";
        };
    }
}
