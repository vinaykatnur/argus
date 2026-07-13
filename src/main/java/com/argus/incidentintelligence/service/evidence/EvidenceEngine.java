package com.argus.incidentintelligence.service.evidence;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.HistoryPoint;
import com.argus.incidentintelligence.dto.IncidentContext;
import com.argus.incidentintelligence.enums.EvidenceType;

@Service
public class EvidenceEngine {

    public List<EvidenceItemDto> extract(IncidentContext context) {
        Map<String, EvidenceItemDto> evidence = new LinkedHashMap<>();
        Instant seed = context.timelineSeed() != null ? context.timelineSeed() : context.incident().startedAt();

        add(evidence, new EvidenceItemDto(
                "incident-opened",
                EvidenceType.OPERATIONAL,
                "Positive evidence: incident opened at " + context.incident().startedAt() + ".",
                "Incident",
                context.incident().startedAt(),
                9
        ));

        if (StringUtils.hasText(context.incident().failureReason())) {
            add(evidence, new EvidenceItemDto(
                    "incident-failure-reason",
                    EvidenceType.OPERATIONAL,
                    "Positive evidence: incident recorded failure reason " + context.incident().failureReason() + ".",
                    "Incident",
                    context.incident().startedAt(),
                    10
            ));
        }

        context.responseTrend().stream()
                .filter(point -> !point.successful())
                .findFirst()
                .ifPresent(point -> add(evidence, failedCheckEvidence(point)));

        responseIncrease(context).ifPresent(item -> add(evidence, item));

        if (context.monitor().failureThreshold() > 0 && context.incident().consecutiveFailedChecks() != null
                && context.incident().consecutiveFailedChecks() >= context.monitor().failureThreshold()) {
            add(evidence, new EvidenceItemDto(
                    "threshold-crossed",
                    EvidenceType.TIMELINE,
                    "Positive evidence: the monitor passed its configured failure threshold with "
                            + context.incident().consecutiveFailedChecks() + " consecutive failed checks.",
                    "Monitor",
                    context.incident().startedAt(),
                    8
            ));
        }

        long failedChecks = context.responseTrend().stream().filter(point -> !point.successful()).count();
        if (failedChecks > 0) {
            add(evidence, new EvidenceItemDto(
                    "failed-check-count",
                    EvidenceType.TIMELINE,
                    "Positive evidence: " + failedChecks + " failed monitoring check(s) were present in the incident window.",
                    "MonitorHistory",
                    context.incident().startedAt(),
                    Math.min(10, 4 + (int) failedChecks)
            ));
        }

        if (context.analyticsAvailable()) {
            add(evidence, new EvidenceItemDto(
                    "analytics-availability",
                    EvidenceType.ANALYTICS,
                    "Positive evidence: thirty-day monitor availability was "
                            + String.format("%.2f", context.analytics().availabilityPercent()) + "%.",
                    "AnalyticsSummary",
                    seed,
                    6
            ));
            if (context.analytics().incidentCount() > 0) {
                add(evidence, new EvidenceItemDto(
                        "analytics-incident-frequency",
                        EvidenceType.ANALYTICS,
                        "Positive evidence: analytics counted " + context.analytics().incidentCount()
                                + " incident(s) in the recent analytics window.",
                        "AnalyticsSummary",
                        seed,
                        7
                ));
            }
        } else {
            add(evidence, new EvidenceItemDto(
                    "analytics-unavailable",
                    EvidenceType.ANALYTICS,
                    "Negative evidence: analytics summary was unavailable for the recent comparison window.",
                    "AnalyticsSummary",
                    seed,
                    3
            ));
        }

        add(evidence, new EvidenceItemDto(
                "monitor-configuration",
                EvidenceType.CONFIGURATION,
                "Configuration evidence: monitor checks " + context.monitor().protocol() + " every "
                        + context.monitor().intervalSeconds() + " seconds with failure threshold "
                        + context.monitor().failureThreshold() + ".",
                "Monitor",
                context.monitor().lastCheckedAt(),
                5
        ));

        if (!context.historicalIncidents().isEmpty()) {
            add(evidence, new EvidenceItemDto(
                    "historical-incidents",
                    EvidenceType.HISTORICAL,
                    "Historical evidence: " + context.historicalIncidents().size()
                            + " historical incident(s) were available for deterministic comparison.",
                    "IncidentHistory",
                    seed,
                    6
            ));
        }

        if (context.incident().resolvedAt() != null) {
            long millis = Duration.between(context.incident().startedAt(), context.incident().resolvedAt()).toMillis();
            add(evidence, new EvidenceItemDto(
                    "incident-resolved",
                    EvidenceType.TIMELINE,
                    "Positive evidence: incident resolved after " + millis + " ms.",
                    "Incident",
                    context.incident().resolvedAt(),
                    8
            ));
        }

        return evidence.values().stream()
                .sorted(Comparator.comparing(EvidenceItemDto::timestamp)
                        .thenComparingInt(EvidenceItemDto::weight).reversed()
                        .thenComparing(EvidenceItemDto::id))
                .toList();
    }

    private EvidenceItemDto failedCheckEvidence(HistoryPoint point) {
        String status = point.httpStatusCode() == null ? "" : " HTTP status " + point.httpStatusCode() + ".";
        String reason = StringUtils.hasText(point.failureReason()) ? " Reason: " + point.failureReason() + "." : "";
        return new EvidenceItemDto(
                "first-failed-check",
                EvidenceType.OPERATIONAL,
                "Positive evidence: first failed check occurred at " + point.checkedAt() + "." + status + reason,
                "MonitorHistory",
                point.checkedAt(),
                10
        );
    }

    private java.util.Optional<EvidenceItemDto> responseIncrease(IncidentContext context) {
        List<Long> responseTimes = context.responseTrend().stream()
                .filter(HistoryPoint::successful)
                .map(HistoryPoint::responseTimeMillis)
                .filter(time -> time != null && time > 0)
                .toList();
        if (responseTimes.size() < 3) {
            return java.util.Optional.empty();
        }

        long first = responseTimes.getFirst();
        long last = responseTimes.getLast();
        if (last <= first * 2) {
            return java.util.Optional.empty();
        }

        return java.util.Optional.of(new EvidenceItemDto(
                "response-time-increase",
                EvidenceType.PERFORMANCE,
                "Positive evidence: response time increased from " + first + " ms to " + last
                        + " ms during the incident context window.",
                "MonitorHistory",
                context.incident().startedAt(),
                8
        ));
    }

    private void add(Map<String, EvidenceItemDto> evidence, EvidenceItemDto item) {
        evidence.putIfAbsent(item.id(), item);
    }
}
