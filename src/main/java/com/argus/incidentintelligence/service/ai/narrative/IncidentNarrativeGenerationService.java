package com.argus.incidentintelligence.service.ai.narrative;

import com.argus.incidentintelligence.dto.IncidentAnalysisSnapshotDto;
import com.argus.incidentintelligence.dto.NarrativeSnapshotDto;
import com.argus.incidentintelligence.enums.NarrativeGenerationStatus;
import com.argus.incidentintelligence.mapper.IncidentIntelligenceJsonMapper;
import com.argus.incidentintelligence.service.ai.AiProviderConfigurationService;
import com.argus.incidentintelligence.service.ai.provider.AiProvider;
import com.argus.incidentintelligence.service.ai.provider.AiProviderRegistry;
import com.argus.incidentintelligence.service.ai.provider.AiProviderRequest;
import com.argus.incidentintelligence.service.ai.provider.AiProviderResponse;
import com.argus.incidentintelligence.service.ai.prompt.IncidentNarrativePromptBuilder;
import com.argus.incidentintelligence.service.ai.validation.NarrativeResponseValidator;
import com.argus.incidentintelligence.service.ai.validation.NarrativeValidationResult;
import com.argus.incidentintelligence.service.snapshot.IncidentAnalysisSnapshotService;
import com.argus.incidentintelligence.service.snapshot.IncidentNarrativeSnapshotService;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.stereotype.Service;

@Service
public class IncidentNarrativeGenerationService {

    private static final TypeReference<NarrativePayload> PAYLOAD_TYPE = new TypeReference<>() {
    };

    private final IncidentAnalysisSnapshotService analysisSnapshotService;
    private final IncidentNarrativeSnapshotService narrativeSnapshotService;
    private final AiProviderConfigurationService configurationService;
    private final IncidentNarrativePromptBuilder promptBuilder;
    private final AiProviderRegistry providerRegistry;
    private final NarrativeResponseValidator responseValidator;
    private final IncidentIntelligenceJsonMapper jsonMapper;

    public IncidentNarrativeGenerationService(
            IncidentAnalysisSnapshotService analysisSnapshotService,
            IncidentNarrativeSnapshotService narrativeSnapshotService,
            AiProviderConfigurationService configurationService,
            IncidentNarrativePromptBuilder promptBuilder,
            AiProviderRegistry providerRegistry,
            NarrativeResponseValidator responseValidator,
            IncidentIntelligenceJsonMapper jsonMapper
    ) {
        this.analysisSnapshotService = analysisSnapshotService;
        this.narrativeSnapshotService = narrativeSnapshotService;
        this.configurationService = configurationService;
        this.promptBuilder = promptBuilder;
        this.providerRegistry = providerRegistry;
        this.responseValidator = responseValidator;
        this.jsonMapper = jsonMapper;
    }

    public NarrativeSnapshotDto generate(Long ownerId, Long incidentId) {
        IncidentAnalysisSnapshotDto analysis = analysisSnapshotService.latest(ownerId, incidentId);
        return configurationService.resolve(ownerId)
                .map(configuration -> generateWithProvider(ownerId, incidentId, analysis, configuration))
                .orElseGet(() -> narrativeSnapshotService.persist(
                        ownerId,
                        incidentId,
                        analysis.id(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        NarrativeGenerationStatus.UNAVAILABLE,
                        "No AI provider is configured for this owner.",
                        null,
                        null
                ));
    }

    private NarrativeSnapshotDto generateWithProvider(
            Long ownerId,
            Long incidentId,
            IncidentAnalysisSnapshotDto analysis,
            AiProviderConfigurationService.ResolvedAiProviderConfiguration configuration
    ) {
        long startedAt = System.nanoTime();
        try {
            AiProvider provider = providerRegistry.get(configuration.providerName());
            AiProviderResponse response = provider.generate(new AiProviderRequest(
                    configuration.modelName(),
                    configuration.apiKey(),
                    promptBuilder.build(analysis)
            ));
            NarrativeValidationResult validation = responseValidator.validate(analysis, response.content());
            if (!validation.valid()) {
                return persistFailure(
                        ownerId,
                        incidentId,
                        analysis,
                        configuration,
                        NarrativeGenerationStatus.VALIDATION_FAILED,
                        String.join(" ", validation.reasons()),
                        elapsedMillis(startedAt),
                        response.tokenUsage()
                );
            }

            NarrativePayload payload = jsonMapper.read(response.content(), PAYLOAD_TYPE);
            return narrativeSnapshotService.persist(
                    ownerId,
                    incidentId,
                    analysis.id(),
                    payload.executiveSummary(),
                    payload.explanation(),
                    payload.markdownPostMortem(),
                    configuration.providerName(),
                    configuration.modelName(),
                    NarrativeGenerationStatus.AVAILABLE,
                    null,
                    elapsedMillis(startedAt),
                    response.tokenUsage()
            );
        } catch (RuntimeException exception) {
            return persistFailure(
                    ownerId,
                    incidentId,
                    analysis,
                    configuration,
                    NarrativeGenerationStatus.FAILED,
                    "AI narrative generation failed.",
                    elapsedMillis(startedAt),
                    null
            );
        }
    }

    private NarrativeSnapshotDto persistFailure(
            Long ownerId,
            Long incidentId,
            IncidentAnalysisSnapshotDto analysis,
            AiProviderConfigurationService.ResolvedAiProviderConfiguration configuration,
            NarrativeGenerationStatus status,
            String failureReason,
            Long generationDurationMillis,
            Integer tokenUsage
    ) {
        return narrativeSnapshotService.persist(
                ownerId,
                incidentId,
                analysis.id(),
                null,
                null,
                null,
                configuration.providerName(),
                configuration.modelName(),
                status,
                failureReason,
                generationDurationMillis,
                tokenUsage
        );
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
