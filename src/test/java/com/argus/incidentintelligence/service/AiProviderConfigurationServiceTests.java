package com.argus.incidentintelligence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.argus.entity.User;
import com.argus.enums.Role;
import com.argus.incidentintelligence.dto.AiProviderConfigurationRequest;
import com.argus.incidentintelligence.dto.AiProviderConfigurationResponse;
import com.argus.incidentintelligence.dto.AiProviderValidationResponse;
import com.argus.incidentintelligence.entity.AiProviderConfiguration;
import com.argus.incidentintelligence.enums.AiProviderName;
import com.argus.incidentintelligence.repository.AiProviderConfigurationRepository;
import com.argus.incidentintelligence.service.ai.AiCredentialEncryptionService;
import com.argus.incidentintelligence.service.ai.AiProviderConfigurationService;
import com.argus.incidentintelligence.service.ai.provider.AiProvider;
import com.argus.incidentintelligence.service.ai.provider.AiProviderRegistry;
import com.argus.incidentintelligence.service.ai.provider.AiProviderResponse;
import com.argus.repository.UserRepository;

class AiProviderConfigurationServiceTests {

    private AiProviderConfigurationRepository repository;
    private UserRepository userRepository;
    private AiCredentialEncryptionService encryptionService;
    private AiProviderRegistry providerRegistry;
    private AiProviderConfigurationService service;

    @BeforeEach
    void setUp() {
        repository = mock(AiProviderConfigurationRepository.class);
        userRepository = mock(UserRepository.class);
        encryptionService = mock(AiCredentialEncryptionService.class);
        providerRegistry = mock(AiProviderRegistry.class);
        service = new AiProviderConfigurationService(repository, userRepository, encryptionService, providerRegistry);
    }

    @Test
    void validateDoesNotPersistKey() {
        AiProviderConfigurationRequest request = new AiProviderConfigurationRequest(
                AiProviderName.OPENAI,
                "gpt-4",
                "sk-dummy-key"
        );
        AiProvider mockProvider = mock(AiProvider.class);
        when(providerRegistry.get(AiProviderName.OPENAI)).thenReturn(mockProvider);
        when(mockProvider.generate(any())).thenReturn(new AiProviderResponse("ARGUS_OK", 10));

        AiProviderValidationResponse response = service.validate(request);

        assertThat(response.valid()).isTrue();
        assertThat(response.status()).isEqualTo("VALID");
        
        verify(repository, never()).save(any());
    }

    @Test
    void toResponseMasksApiKey() {
        User user = new User("Test User", "test@example.com", "hash", Role.USER);
        user.setId(1L);

        AiProviderConfiguration configuration = new AiProviderConfiguration(
                user,
                AiProviderName.OPENAI,
                "gpt-4",
                "encrypted-secret-key"
        );

        when(repository.findByOwner_Id(1L)).thenReturn(Optional.of(configuration));

        AiProviderConfigurationResponse response = service.get(1L);

        assertThat(response.providerName()).isEqualTo(AiProviderName.OPENAI);
        assertThat(response.modelName()).isEqualTo("gpt-4");
        assertThat(response.configured()).isTrue();
    }
}