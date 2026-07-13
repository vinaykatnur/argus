package com.argus.incidentintelligence.service.ai.prompt;

import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.IncidentAnalysisSnapshotDto;
import com.argus.incidentintelligence.dto.RecommendationDto;
import com.argus.incidentintelligence.dto.TimelineEventDto;
import org.springframework.stereotype.Service;

@Service
public class IncidentNarrativePromptBuilder {

    public String build(IncidentAnalysisSnapshotDto snapshot) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("""
                You are writing an incident narrative for ARGUS.
                You are not the source of truth. The deterministic analysis below is the only source of truth.
                Do not add root causes, services, infrastructure, vendors, metrics, actions, confidence, or recommendations that are not present below.
                If evidence is insufficient, say so plainly.
                Return strict JSON with these fields only:
                executiveSummary, explanation, markdownPostMortem

                Deterministic analysis:
                """);
        prompt.append("Summary: ").append(snapshot.incidentSummary()).append('\n');
        prompt.append("Probable pattern: ").append(snapshot.probablePattern()).append('\n');
        prompt.append("Confidence: ").append(snapshot.confidence().level()).append(" (")
                .append(snapshot.confidence().score()).append("%)\n");

        prompt.append("\nEvidence:\n");
        for (EvidenceItemDto evidence : snapshot.evidence()) {
            prompt.append("- [").append(evidence.id()).append("] ")
                    .append(evidence.type()).append(": ")
                    .append(evidence.observation()).append('\n');
        }

        prompt.append("\nTimeline:\n");
        for (TimelineEventDto event : snapshot.timeline()) {
            prompt.append("- ").append(event.timestamp()).append(" ")
                    .append(event.eventType()).append(": ")
                    .append(event.description()).append('\n');
        }

        prompt.append("\nRecommendations:\n");
        for (RecommendationDto recommendation : snapshot.recommendations()) {
            prompt.append("- ").append(recommendation.priority()).append(" ")
                    .append(recommendation.category()).append(": ")
                    .append(recommendation.recommendation()).append(" Reason: ")
                    .append(recommendation.reason()).append('\n');
        }

        prompt.append("\nHistorical similarity:\n");
        prompt.append(snapshot.similarity().summary()).append(" Best score: ")
                .append(snapshot.similarity().bestScore()).append("%.\n");

        return prompt.toString();
    }
}
