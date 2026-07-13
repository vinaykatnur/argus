package com.argus.incidentintelligence.dto;

import com.argus.incidentintelligence.enums.AiProviderName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AiProviderConfigurationRequest(
        @NotNull AiProviderName providerName,
        @NotBlank @Size(max = 120) String modelName,
        @NotBlank @Size(max = 4000) String apiKey
) {
}
