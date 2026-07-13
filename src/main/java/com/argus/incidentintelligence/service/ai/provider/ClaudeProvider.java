package com.argus.incidentintelligence.service.ai.provider;

import com.argus.incidentintelligence.config.IncidentIntelligenceProperties;
import com.argus.incidentintelligence.enums.AiProviderName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class ClaudeProvider extends HttpAiProviderSupport {

    public ClaudeProvider(ObjectMapper objectMapper, IncidentIntelligenceProperties properties) {
        super(objectMapper, properties);
    }

    @Override
    public AiProviderName providerName() {
        return AiProviderName.CLAUDE;
    }

    @Override
    public AiProviderResponse generate(AiProviderRequest request) {
        String body = """
                {"model":%s,"max_tokens":1200,"temperature":0.1,"messages":[{"role":"user","content":%s}]}
                """.formatted(jsonString(request.modelName()), jsonString(request.prompt()));
        JsonNode json = postJson(
                URI.create("https://api.anthropic.com/v1/messages"),
                request.apiKey(),
                body,
                new Header("x-api-key", request.apiKey()),
                new Header("anthropic-version", "2023-06-01")
        );
        String content = json.at("/content/0/text").asText("");
        int inputTokens = json.at("/usage/input_tokens").isMissingNode() ? 0 : json.at("/usage/input_tokens").asInt();
        int outputTokens = json.at("/usage/output_tokens").isMissingNode() ? 0 : json.at("/usage/output_tokens").asInt();
        Integer usage = inputTokens + outputTokens == 0 ? null : inputTokens + outputTokens;
        return new AiProviderResponse(content, usage);
    }
}
