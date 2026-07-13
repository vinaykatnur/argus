package com.argus.incidentintelligence.dto;

import java.time.Instant;

public record AiProviderValidationResponse(
        boolean valid,
        String status,
        String message,
        Instant validatedAt
) {
}
