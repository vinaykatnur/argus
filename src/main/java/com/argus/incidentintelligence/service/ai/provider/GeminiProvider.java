package com.argus.incidentintelligence.service.ai.provider;

import com.argus.incidentintelligence.config.IncidentIntelligenceProperties;
import com.argus.incidentintelligence.enums.AiProviderName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class GeminiProvider extends HttpAiProviderSupport {

    public GeminiProvider(ObjectMapper objectMapper, IncidentIntelligenceProperties properties) {
        super(objectMapper, properties);
    }

    @Override
    public AiProviderName providerName() {
        return AiProviderName.GEMINI;
    }

    @Override
    public AiProviderResponse generate(AiProviderRequest request) {
        String body = """
                {"contents":[{"parts":[{"text":%s}]}],"generationConfig":{"temperature":0.1}}
                """.formatted(jsonString(request.prompt()));
        String model = URLEncoder.encode(request.modelName(), StandardCharsets.UTF_8);
        String key = URLEncoder.encode(request.apiKey(), StandardCharsets.UTF_8);
        JsonNode json = postJson(
                URI.create("https://generativelanguage.googleapis.com/v1beta/models/" + model
                        + ":generateContent?key=" + key),
                request.apiKey(),
                body
        );
        String content = json.at("/candidates/0/content/parts/0/text").asText("");
        Integer usage = json.at("/usageMetadata/totalTokenCount").isMissingNode()
                ? null
                : json.at("/usageMetadata/totalTokenCount").asInt();
        return new AiProviderResponse(content, usage);
    }
}
