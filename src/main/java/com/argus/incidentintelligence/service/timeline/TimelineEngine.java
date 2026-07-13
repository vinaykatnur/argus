package com.argus.incidentintelligence.service.timeline;

import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.HistoryPoint;
import com.argus.incidentintelligence.dto.IncidentContext;
import com.argus.incidentintelligence.dto.TimelineEventDto;
import com.argus.incidentintelligence.enums.TimelineEventType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TimelineEngine {

    public List<TimelineEventDto> build(IncidentContext context, List<EvidenceItemDto> evidence) {
        List<TimelineEventDto> timeline = new ArrayList<>();

        degradation(context).ifPresent(timeline::add);
        thresholdCrossed(context).ifPresent(timeline::add);
        firstFailedCheck(context).ifPresent(timeline::add);

        timeline.add(new TimelineEventDto(
                context.incident().startedAt(),
                TimelineEventType.INCIDENT_OPENED,
                "Incident opened for monitor " + displayName(context) + ".",
                evidenceIds(evidence, "incident-opened")
        ));

        recoveryDetected(context).ifPresent(timeline::add);

        if (context.incident().resolvedAt() != null) {
            timeline.add(new TimelineEventDto(
                    context.incident().resolvedAt(),
                    TimelineEventType.INCIDENT_RESOLVED,
                    "Incident resolved after the monitor returned to a healthy state.",
                    evidenceIds(evidence, "incident-resolved")
            ));
        }

        return timeline.stream()
                .sorted(Comparator.comparing(TimelineEventDto::timestamp)
                        .thenComparing(TimelineEventDto::eventType))
                .toList();
    }

    private java.util.Optional<TimelineEventDto> degradation(IncidentContext context) {
        List<HistoryPoint> successful = context.responseTrend().stream()
                .filter(HistoryPoint::successful)
                .filter(point -> point.responseTimeMillis() != null)
                .toList();
        if (successful.size() < 3) {
            return java.util.Optional.empty();
        }

        HistoryPoint first = successful.getFirst();
        HistoryPoint last = successful.getLast();
        if (last.responseTimeMillis() <= first.responseTimeMillis() * 2) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new TimelineEventDto(
                last.checkedAt(),
                TimelineEventType.PERFORMANCE_DEGRADATION,
                "Response time increased from " + first.responseTimeMillis()
                        + " ms to " + last.responseTimeMillis() + " ms.",
                List.of("response-time-increase")
        ));
    }

    private java.util.Optional<TimelineEventDto> thresholdCrossed(IncidentContext context) {
        Integer threshold = context.monitor().failureThreshold();
        Integer consecutive = context.incident().consecutiveFailedChecks();
        if (threshold == null || consecutive == null || consecutive < threshold) {
            return java.util.Optional.empty();
        }
        return java.util.Optional.of(new TimelineEventDto(
                context.incident().startedAt(),
                TimelineEventType.THRESHOLD_EXCEEDED,
                "Failure threshold was crossed after " + consecutive + " consecutive failed checks.",
                List.of("threshold-crossed")
        ));
    }

    private java.util.Optional<TimelineEventDto> firstFailedCheck(IncidentContext context) {
        return context.responseTrend().stream()
                .filter(point -> !point.successful())
                .findFirst()
                .map(point -> new TimelineEventDto(
                        point.checkedAt(),
                        TimelineEventType.FIRST_FAILED_CHECK,
                        "First failed check observed in the incident context window.",
                        List.of("first-failed-check")
                ));
    }

    private java.util.Optional<TimelineEventDto> recoveryDetected(IncidentContext context) {
        if (context.incident().resolvedAt() == null) {
            return java.util.Optional.empty();
        }
        return context.responseTrend().stream()
                .filter(HistoryPoint::successful)
                .filter(point -> !point.checkedAt().isBefore(context.incident().startedAt()))
                .filter(point -> !point.checkedAt().isAfter(context.incident().resolvedAt()))
                .findFirst()
                .map(point -> new TimelineEventDto(
                        point.checkedAt(),
                        TimelineEventType.RECOVERY_DETECTED,
                        "Successful check observed before incident resolution.",
                        List.of()
                ));
    }

    private List<String> evidenceIds(List<EvidenceItemDto> evidence, String id) {
        return evidence.stream()
                .map(EvidenceItemDto::id)
                .filter(current -> current.equals(id))
                .toList();
    }

    private String displayName(IncidentContext context) {
        return context.monitor().displayName() == null ? context.monitor().url() : context.monitor().displayName();
    }
}
