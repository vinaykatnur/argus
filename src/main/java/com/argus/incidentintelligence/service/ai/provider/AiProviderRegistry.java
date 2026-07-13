package com.argus.incidentintelligence.service.ai.provider;

import com.argus.exception.ApiException;
import com.argus.incidentintelligence.enums.AiProviderName;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AiProviderRegistry {

    private final Map<AiProviderName, AiProvider> providers = new EnumMap<>(AiProviderName.class);

    public AiProviderRegistry(List<AiProvider> providers) {
        providers.forEach(provider -> this.providers.put(provider.providerName(), provider));
    }

    public AiProvider get(AiProviderName providerName) {
        AiProvider provider = providers.get(providerName);
        if (provider == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "AI provider is not supported");
        }
        return provider;
    }
}
