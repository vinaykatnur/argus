package com.argus.incidentintelligence.controller;

import com.argus.incidentintelligence.dto.AiProviderConfigurationRequest;
import com.argus.incidentintelligence.dto.AiProviderConfigurationResponse;
import com.argus.incidentintelligence.dto.AiProviderValidationResponse;
import com.argus.incidentintelligence.service.ai.AiProviderConfigurationService;
import com.argus.response.MessageResponse;
import com.argus.security.ArgusUserDetails;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/me/ai-provider")
public class AiProviderConfigurationController {

    private final AiProviderConfigurationService configurationService;

    public AiProviderConfigurationController(AiProviderConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @GetMapping
    public ResponseEntity<AiProviderConfigurationResponse> get(
            @AuthenticationPrincipal ArgusUserDetails userDetails
    ) {
        return ResponseEntity.ok(configurationService.get(userDetails.getUser().getId()));
    }

    @PutMapping
    public ResponseEntity<AiProviderConfigurationResponse> configure(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @Valid @RequestBody AiProviderConfigurationRequest request
    ) {
        return ResponseEntity.ok(configurationService.configure(userDetails.getUser().getId(), request));
    }

    @PostMapping("/validate")
    public ResponseEntity<AiProviderValidationResponse> validate(
            @Valid @RequestBody AiProviderConfigurationRequest request
    ) {
        return ResponseEntity.ok(configurationService.validate(request));
    }

    @DeleteMapping
    public ResponseEntity<MessageResponse> delete(
            @AuthenticationPrincipal ArgusUserDetails userDetails
    ) {
        configurationService.delete(userDetails.getUser().getId());
        return ResponseEntity.ok(new MessageResponse("AI provider configuration deleted"));
    }
}
