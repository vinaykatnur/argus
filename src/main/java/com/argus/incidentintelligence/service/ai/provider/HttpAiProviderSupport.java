package com.argus.incidentintelligence.service.ai.provider;

import com.argus.exception.ApiException;
import com.argus.incidentintelligence.config.IncidentIntelligenceProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.http.HttpStatus;

abstract class HttpAiProviderSupport implements AiProvider {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final IncidentIntelligenceProperties properties;

    protected HttpAiProviderSupport(ObjectMapper objectMapper, IncidentIntelligenceProperties properties) {
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(properties.ai().timeoutSeconds()))
                .build();
    }

    protected JsonNode postJson(URI uri, String apiKey, String requestBody, Header... headers) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofSeconds(properties.ai().timeoutSeconds()))
                    .header("Content-Type", "application/json");
            for (Header header : headers) {
                builder.header(header.name(), header.value());
            }
            HttpRequest request = builder
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(HttpStatus.BAD_GATEWAY, "AI provider request failed");
            }
            return objectMapper.readTree(response.body());
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI provider response could not be read");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException(HttpStatus.BAD_GATEWAY, "AI provider request was interrupted");
        }
    }

    protected String jsonString(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to build AI provider request");
        }
    }

    protected record Header(String name, String value) {
    }
}
