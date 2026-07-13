package com.argus.response;

import com.argus.entity.Incident;
import java.time.Instant;
import java.util.Map;

public record IncidentTimelineDetailsResponse(
        String failureReason,
        int failureThreshold,
        int failedCheckCount,
        Instant lastSuccessfulCheck,
        Instant recoveryTime,
        Map<String, Object> diagnosticMetadata
) {

    public static IncidentTimelineDetailsResponse from(Incident incident) {
        return new IncidentTimelineDetailsResponse(
                currentFailureReason(incident),
                incident.getMonitor().getFailureThreshold(),
                incident.getMonitor().getConsecutiveFailureCount(),
                incident.getMonitor().getLastSuccessfulCheckAt(),
                incident.getResolvedAt(),
                Map.of(
                        "currentStatus", incident.getMonitor().getStatus(),
                        "currentResponseTimeMillis", nullableValue(incident.getMonitor().getLastResponseTimeMillis()),
                        "lastCheckedAt", nullableValue(incident.getMonitor().getLastCheckedAt())
                )
        );
    }

    private static String currentFailureReason(Incident incident) {
        if (incident.getMonitor().getConsecutiveFailureCount() > 0) {
            return "Monitor failed recent health checks";
        }
        return "Incident was recorded by the monitoring engine";
    }

    private static Object nullableValue(Object value) {
        return value == null ? "UNKNOWN" : value;
    }
}
