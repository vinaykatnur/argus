package com.argus.service;

import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import com.argus.entity.Notification;
import com.argus.enums.NotificationType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.util.HtmlUtils;

@Service
public class NotificationTemplateService {

    private final ResourceLoader resourceLoader;

    public NotificationTemplateService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public NotificationContent render(Notification notification) {
        String html = loadTemplate(notification.getType());
        Map<String, String> values = model(notification);
        for (Map.Entry<String, String> entry : values.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", HtmlUtils.htmlEscape(entry.getValue()));
        }
        return new NotificationContent(subject(notification), html, plainText(html));
    }

    private String loadTemplate(NotificationType type) {
        String path = switch (type) {
            case WEBSITE_DOWN -> "classpath:templates/notifications/website-down.html";
            case WEBSITE_DOWN_REMINDER -> "classpath:templates/notifications/website-down-reminder.html";
            case WEBSITE_RECOVERED -> "classpath:templates/notifications/website-recovered.html";
            case SLOW_RESPONSE -> "classpath:templates/notifications/slow-response.html";
        };
        Resource resource = resourceLoader.getResource(path);
        try (var inputStream = resource.getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load notification template " + path, exception);
        }
    }

    private Map<String, String> model(Notification notification) {
        Monitor monitor = notification.getMonitor();
        Incident incident = notification.getIncident();
        Map<String, String> values = new LinkedHashMap<>();
        values.put("monitorName", displayName(monitor));
        values.put("monitorUrl", monitor.getUrl());
        values.put("ownerName", monitor.getOwner().getName());
        values.put("eventTime", value(notification.getEventTime()));
        values.put("failureReason", fallback(notification.getFailureReason(), "Monitor failed recent health checks"));
        values.put("responseTimeMillis", value(notification.getResponseTimeMillis()));
        values.put("incidentId", incident == null ? "N/A" : value(incident.getId()));
        values.put("incidentStart", incident == null ? value(notification.getEventTime()) : value(incident.getStartedAt()));
        values.put("recoveryTime", incident == null ? "N/A" : value(incident.getResolvedAt()));
        values.put("downtimeDuration", incident == null ? "N/A" : duration(incident));
        values.put("consecutiveFailedChecks", incident == null
                ? value(monitor.getConsecutiveFailureCount())
                : value(incident.getConsecutiveFailedChecks()));
        values.put("lastSuccessfulCheck", incident == null
                ? value(monitor.getLastSuccessfulCheckAt())
                : value(incident.getLastSuccessfulCheckAt()));
        return values;
    }

    private String subject(Notification notification) {
        Monitor monitor = notification.getMonitor();
        return switch (notification.getType()) {
            case WEBSITE_DOWN -> "ARGUS alert: %s is down".formatted(displayName(monitor));
            case WEBSITE_DOWN_REMINDER -> "ARGUS reminder: %s is still down".formatted(displayName(monitor));
            case WEBSITE_RECOVERED -> "ARGUS recovery: %s is back online".formatted(displayName(monitor));
            case SLOW_RESPONSE -> "ARGUS alert: %s is responding slowly".formatted(displayName(monitor));
        };
    }

    private String plainText(String html) {
        return html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</p>", "\n\n")
                .replaceAll("<[^>]+>", "")
                .replace("&nbsp;", " ")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .trim();
    }

    private String displayName(Monitor monitor) {
        return monitor.getDisplayName() == null ? "Unnamed monitor" : monitor.getDisplayName();
    }

    private String duration(Incident incident) {
        Long downtimeMillis = incident.getDowntimeMillis();
        if (downtimeMillis == null && incident.getResolvedAt() != null) {
            downtimeMillis = Duration.between(incident.getStartedAt(), incident.getResolvedAt()).toMillis();
        }
        if (downtimeMillis == null) {
            downtimeMillis = Duration.between(incident.getStartedAt(), Instant.now()).toMillis();
        }
        long seconds = downtimeMillis / 1000;
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;
        return "%d min %d sec".formatted(minutes, remainingSeconds);
    }

    private String fallback(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String value(Object value) {
        return value == null ? "N/A" : value.toString();
    }
}
