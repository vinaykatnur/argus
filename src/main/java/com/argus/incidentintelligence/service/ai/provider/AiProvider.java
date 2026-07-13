package com.argus.incidentintelligence.service.ai.provider;

import com.argus.incidentintelligence.enums.AiProviderName;

public interface AiProvider {

    AiProviderName providerName();

    AiProviderResponse generate(AiProviderRequest request);
}
