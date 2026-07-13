package com.argus.incidentintelligence.service.ai.provider;

import com.argus.incidentintelligence.config.IncidentIntelligenceProperties;
import com.argus.incidentintelligence.enums.AiProviderName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import org.springframework.stereotype.Component;

@Component
public class OpenAIProvider extends HttpAiProviderSupport {

    public OpenAIProvider(ObjectMapper objectMapper, IncidentIntelligenceProperties properties) {
        super(objectMapper, properties);
    }

    @Override
    public AiProviderName providerName() {
        return AiProviderName.OPENAI;
    }

    @Override
    public AiProviderResponse generate(AiProviderRequest request) {
        String body = """
                {"model":%s,"messages":[{"role":"user","content":%s}],"temperature":0.1}
                """.formatted(jsonString(request.modelName()), jsonString(request.prompt()));
        JsonNode json = postJson(
                URI.create("https://api.openai.com/v1/chat/completions"),
                request.apiKey(),
                body,
                new Header("Authorization", "Bearer " + request.apiKey())
        );
        String content = json.at("/choices/0/message/content").asText("");
        Integer usage = json.at("/usage/total_tokens").isMissingNode() ? null : json.at("/usage/total_tokens").asInt();
        return new AiProviderResponse(content, usage);
    }
}
