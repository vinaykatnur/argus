package com.argus.response;

import com.argus.entity.Incident;
import com.argus.enums.IncidentStatus;
import java.time.Duration;
import java.time.Instant;

public record IncidentTimelineItemResponse(
        Long id,
        Long monitorId,
        String monitorName,
        String monitorUrl,
        IncidentStatus status,
        Instant startedAt,
        Instant recoveredAt,
        Long downtimeMillis,
        IncidentTimelineDetailsResponse details
) {

    public static IncidentTimelineItemResponse from(Incident incident) {
        return new IncidentTimelineItemResponse(
                incident.getId(),
                incident.getMonitor().getId(),
                incident.getMonitor().getDisplayName(),
                incident.getMonitor().getUrl(),
                incident.getStatus(),
                incident.getStartedAt(),
                incident.getResolvedAt(),
                resolveDowntimeMillis(incident),
                IncidentTimelineDetailsResponse.from(incident)
        );
    }

    private static Long resolveDowntimeMillis(Incident incident) {
        if (incident.getDowntimeMillis() != null) {
            return incident.getDowntimeMillis();
        }
        if (incident.getResolvedAt() == null) {
            return Duration.between(incident.getStartedAt(), Instant.now()).toMillis();
        }
        return Duration.between(incident.getStartedAt(), incident.getResolvedAt()).toMillis();
    }
}
