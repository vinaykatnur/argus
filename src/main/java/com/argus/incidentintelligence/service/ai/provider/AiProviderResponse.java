package com.argus.incidentintelligence.service.ai.provider;

public record AiProviderResponse(
        String content,
        Integer tokenUsage
) {
}
