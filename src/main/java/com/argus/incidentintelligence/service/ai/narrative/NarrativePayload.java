package com.argus.incidentintelligence.service.ai.narrative;

public record NarrativePayload(
        String executiveSummary,
        String explanation,
        String markdownPostMortem
) {
}
