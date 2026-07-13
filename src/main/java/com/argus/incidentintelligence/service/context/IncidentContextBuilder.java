package com.argus.incidentintelligence.service.context;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.argus.entity.AnalyticsSummary;
import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import com.argus.entity.MonitorHistory;
import com.argus.exception.ResourceNotFoundException;
import com.argus.incidentintelligence.dto.AnalyticsContext;
import com.argus.incidentintelligence.dto.HistoricalIncidentContext;
import com.argus.incidentintelligence.dto.HistoryPoint;
import com.argus.incidentintelligence.dto.IncidentContext;
import com.argus.incidentintelligence.dto.IncidentFacts;
import com.argus.incidentintelligence.dto.MonitorContext;
import com.argus.repository.AnalyticsSummaryRepository;
import com.argus.repository.IncidentRepository;
import com.argus.repository.MonitorHistoryRepository;

@Service
public class IncidentContextBuilder {

    private static final int HISTORY_LOOKBACK_HOURS = 2;
    private static final int ANALYTICS_LOOKBACK_DAYS = 30;

    private final IncidentRepository incidentRepository;
    private final MonitorHistoryRepository monitorHistoryRepository;
    private final AnalyticsSummaryRepository analyticsSummaryRepository;

    public IncidentContextBuilder(
            IncidentRepository incidentRepository,
            MonitorHistoryRepository monitorHistoryRepository,
            AnalyticsSummaryRepository analyticsSummaryRepository
    ) {
        this.incidentRepository = incidentRepository;
        this.monitorHistoryRepository = monitorHistoryRepository;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
    }

    @Transactional(readOnly = true)
    public IncidentContext build(Long ownerId, Long incidentId) {
        Incident incident = incidentRepository.findByIdAndMonitor_Owner_Id(incidentId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        Monitor monitor = incident.getMonitor();

        Instant historyStart = incident.getStartedAt().minus(HISTORY_LOOKBACK_HOURS, ChronoUnit.HOURS);
        Instant historyEnd = incident.getResolvedAt() != null
                ? incident.getResolvedAt().plus(30, ChronoUnit.MINUTES)
                : incident.getStartedAt().plus(2, ChronoUnit.HOURS);
        List<HistoryPoint> history = monitorHistoryRepository
                .findByMonitor_IdAndCheckedAtBetweenOrderByCheckedAtAsc(monitor.getId(), historyStart, historyEnd)
                .stream()
                .map(this::toHistoryPoint)
                .toList();

        LocalDate analyticsEnd = incident.getResolvedAt() != null
                ? incident.getResolvedAt().atZone(ZoneOffset.UTC).toLocalDate()
                : incident.getStartedAt().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate analyticsStart = analyticsEnd.minusDays(ANALYTICS_LOOKBACK_DAYS);
        List<AnalyticsSummary> summaries = analyticsSummaryRepository
                .findByMonitor_IdAndDateBetweenOrderByDateAsc(monitor.getId(), analyticsStart, analyticsEnd);

        List<HistoricalIncidentContext> historicalIncidents = incidentRepository
                .findTop20ByMonitor_Owner_IdAndIdNotOrderByStartedAtDesc(ownerId, incidentId)
                .stream()
                .map(this::toHistoricalIncident)
                .toList();

        return new IncidentContext(
                toMonitorContext(monitor),
                toIncidentFacts(incident),
                toAnalyticsContext(summaries, analyticsStart, analyticsEnd),
                history,
                historicalIncidents,
                historicalIncidents.stream()
                        .filter(HistoricalIncidentContext::resolved)
                        .map(HistoricalIncidentContext::failureReason)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .limit(10)
                        .toList(),
                incident.getStartedAt(),
                !summaries.isEmpty(),
                !history.isEmpty()
        );
    }

    private MonitorContext toMonitorContext(Monitor monitor) {
        return new MonitorContext(
                monitor.getId(),
                monitor.getOwner().getId(),
                monitor.getDisplayName(),
                monitor.getUrl(),
                protocol(monitor.getUrl()),
                monitor.getIntervalSeconds(),
                monitor.getFailureThreshold(),
                monitor.getStatus(),
                monitor.getConsecutiveFailureCount(),
                monitor.getLastCheckedAt(),
                monitor.getLastSuccessfulCheckAt(),
                monitor.getLastResponseTimeMillis()
        );
    }

    private IncidentFacts toIncidentFacts(Incident incident) {
        return new IncidentFacts(
                incident.getId(),
                incident.getStatus(),
                incident.getStartedAt(),
                incident.getResolvedAt(),
                incident.getDowntimeMillis(),
                incident.getFailureReason(),
                incident.getConsecutiveFailedChecks(),
                incident.getLastSuccessfulCheckAt()
        );
    }

    private HistoryPoint toHistoryPoint(MonitorHistory history) {
        return new HistoryPoint(
                history.getCheckedAt(),
                history.isSuccessful(),
                history.getHttpStatusCode(),
                history.getResponseTimeMillis(),
                history.getFailureReason()
        );
    }

    private HistoricalIncidentContext toHistoricalIncident(Incident incident) {
        Monitor monitor = incident.getMonitor();
        return new HistoricalIncidentContext(
                incident.getId(),
                monitor.getId(),
                monitor.getDisplayName(),
                monitor.getUrl(),
                protocol(monitor.getUrl()),
                incident.getStatus(),
                incident.getStartedAt(),
                incident.getResolvedAt(),
                incident.getDowntimeMillis(),
                incident.getFailureReason(),
                incident.getConsecutiveFailedChecks(),
                incident.getResolvedAt() != null
        );
    }

    private AnalyticsContext toAnalyticsContext(
            List<AnalyticsSummary> summaries,
            LocalDate analyticsStart,
            LocalDate analyticsEnd
    ) {
        if (summaries.isEmpty()) {
            return AnalyticsContext.unavailable(analyticsStart, analyticsEnd);
        }

        long totalChecks = summaries.stream().mapToLong(AnalyticsSummary::getTotalChecks).sum();
        long successfulChecks = summaries.stream().mapToLong(AnalyticsSummary::getSuccessfulChecks).sum();
        long responseCount = summaries.stream().mapToLong(AnalyticsSummary::getResponseCount).sum();
        long responseSum = summaries.stream().mapToLong(AnalyticsSummary::getSumResponseTimeMillis).sum();
        long incidents = summaries.stream().mapToLong(AnalyticsSummary::getIncidentCount).sum();
        long downtime = summaries.stream().mapToLong(AnalyticsSummary::getTotalDowntimeMillis).sum();
        long incidentDuration = incidents == 0 ? 0 : downtime / incidents;
        long mtbf = Math.round(summaries.stream().mapToLong(AnalyticsSummary::getMtbfMillis).average().orElse(0));
        double availability = totalChecks == 0 ? 0 : (successfulChecks * 100.0) / totalChecks;
        long averageResponse = responseCount == 0 ? 0 : responseSum / responseCount;

        return new AnalyticsContext(
                true,
                analyticsStart,
                analyticsEnd,
                totalChecks,
                successfulChecks,
                availability,
                averageResponse,
                incidents,
                downtime,
                incidentDuration,
                mtbf
        );
    }

    private String protocol(String url) {
        try {
            String scheme = URI.create(url).getScheme();
            return StringUtils.hasText(scheme) ? scheme.toUpperCase(Locale.ROOT) : "UNKNOWN";
        } catch (IllegalArgumentException exception) {
            return "UNKNOWN";
        }
    }
}
