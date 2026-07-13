package com.argus.service;

import com.argus.config.MonitorProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.springframework.stereotype.Service;

@Service
public class HealthCheckExecutor {

    private static final String USER_AGENT = "ARGUS-Monitor/1.0";

    private final HttpClient httpClient;
    private final MonitorProperties monitorProperties;

    public HealthCheckExecutor(MonitorProperties monitorProperties) {
        this.monitorProperties = monitorProperties;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(monitorProperties.getRequestTimeoutSeconds()))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public HealthCheckResult check(String url) {
        long startedNanos = System.nanoTime();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofSeconds(monitorProperties.getRequestTimeoutSeconds()))
                .header("User-Agent", USER_AGENT)
                .GET()
                .build();

        try {
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            long responseTimeMillis = elapsedMillis(startedNanos);
            boolean successful = response.statusCode() >= 200 && response.statusCode() < 300;
            return new HealthCheckResult(successful, response.statusCode(), responseTimeMillis, null);
        } catch (IOException exception) {
            return new HealthCheckResult(false, null, elapsedMillis(startedNanos), exception.getClass().getSimpleName());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new HealthCheckResult(false, null, elapsedMillis(startedNanos), "Interrupted");
        } catch (RuntimeException exception) {
            return new HealthCheckResult(false, null, elapsedMillis(startedNanos), exception.getClass().getSimpleName());
        }
    }

    private long elapsedMillis(long startedNanos) {
        return Duration.ofNanos(System.nanoTime() - startedNanos).toMillis();
    }
}
