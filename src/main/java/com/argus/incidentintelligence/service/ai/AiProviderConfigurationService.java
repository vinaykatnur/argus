package com.argus.incidentintelligence.service.ai;

import com.argus.entity.User;
import com.argus.exception.ResourceNotFoundException;
import com.argus.incidentintelligence.dto.AiProviderConfigurationRequest;
import com.argus.incidentintelligence.dto.AiProviderConfigurationResponse;
import com.argus.incidentintelligence.dto.AiProviderValidationResponse;
import com.argus.incidentintelligence.entity.AiProviderConfiguration;
import com.argus.incidentintelligence.repository.AiProviderConfigurationRepository;
import com.argus.incidentintelligence.service.ai.provider.AiProviderRequest;
import com.argus.incidentintelligence.service.ai.provider.AiProviderRegistry;
import com.argus.repository.UserRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AiProviderConfigurationService {

    private final AiProviderConfigurationRepository repository;
    private final UserRepository userRepository;
    private final AiCredentialEncryptionService encryptionService;
    private final AiProviderRegistry providerRegistry;

    public AiProviderConfigurationService(
            AiProviderConfigurationRepository repository,
            UserRepository userRepository,
            AiCredentialEncryptionService encryptionService,
            AiProviderRegistry providerRegistry
    ) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
        this.providerRegistry = providerRegistry;
    }

    @Transactional(readOnly = true)
    public AiProviderConfigurationResponse get(Long ownerId) {
        return repository.findByOwner_Id(ownerId)
                .map(this::toResponse)
                .orElse(new AiProviderConfigurationResponse(null, null, false, null));
    }

    @Transactional
    public AiProviderConfigurationResponse configure(Long ownerId, AiProviderConfigurationRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String encryptedKey = encryptionService.encrypt(request.apiKey());
        AiProviderConfiguration configuration = repository.findByOwner_Id(ownerId)
                .orElseGet(() -> new AiProviderConfiguration(
                        owner,
                        request.providerName(),
                        request.modelName(),
                        encryptedKey
                ));
        configuration.update(request.providerName(), request.modelName(), encryptedKey);
        return toResponse(repository.save(configuration));
    }

    @Transactional
    public void delete(Long ownerId) {
        repository.deleteByOwner_Id(ownerId);
    }

    public AiProviderValidationResponse validate(AiProviderConfigurationRequest request) {
        try {
            providerRegistry.get(request.providerName()).generate(new AiProviderRequest(
                    request.modelName(),
                    request.apiKey(),
                    "Respond with ARGUS_OK only."
            ));
            return new AiProviderValidationResponse(
                    true,
                    "VALID",
                    "AI provider connection validated.",
                    Instant.now()
            );
        } catch (RuntimeException exception) {
            return new AiProviderValidationResponse(
                    false,
                    "FAILED",
                    exception.getMessage() == null ? "AI provider validation failed." : exception.getMessage(),
                    Instant.now()
            );
        }
    }

    @Transactional(readOnly = true)
    public java.util.Optional<ResolvedAiProviderConfiguration> resolve(Long ownerId) {
        return repository.findByOwner_Id(ownerId)
                .map(configuration -> new ResolvedAiProviderConfiguration(
                        configuration.getProviderName(),
                        configuration.getModelName(),
                        encryptionService.decrypt(configuration.getEncryptedApiKey())
                ));
    }

    private AiProviderConfigurationResponse toResponse(AiProviderConfiguration configuration) {
        return new AiProviderConfigurationResponse(
                configuration.getProviderName(),
                configuration.getModelName(),
                true,
                configuration.getUpdatedAt()
        );
    }

    public record ResolvedAiProviderConfiguration(
            com.argus.incidentintelligence.enums.AiProviderName providerName,
            String modelName,
            String apiKey
    ) {
    }
}
