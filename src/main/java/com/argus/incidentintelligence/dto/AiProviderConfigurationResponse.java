package com.argus.incidentintelligence.dto;

import com.argus.incidentintelligence.enums.AiProviderName;
import java.time.Instant;

public record AiProviderConfigurationResponse(
        AiProviderName providerName,
        String modelName,
        boolean configured,
        Instant updatedAt
) {
}
