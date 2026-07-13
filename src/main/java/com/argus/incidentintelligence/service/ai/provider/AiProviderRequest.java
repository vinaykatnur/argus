package com.argus.incidentintelligence.service.ai.provider;

public record AiProviderRequest(
        String modelName,
        String apiKey,
        String prompt
) {
}
