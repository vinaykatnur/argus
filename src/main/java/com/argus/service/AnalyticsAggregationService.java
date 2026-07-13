package com.argus.service;

import com.argus.entity.AnalyticsSummary;
import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import com.argus.entity.MonitorHistory;
import com.argus.repository.AnalyticsSummaryRepository;
import com.argus.repository.IncidentRepository;
import com.argus.repository.MonitorHistoryRepository;
import com.argus.repository.MonitorRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsAggregationService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsAggregationService.class);

    private final MonitorRepository monitorRepository;
    private final MonitorHistoryRepository monitorHistoryRepository;
    private final IncidentRepository incidentRepository;
    private final AnalyticsSummaryRepository analyticsSummaryRepository;

    public AnalyticsAggregationService(
            MonitorRepository monitorRepository,
            MonitorHistoryRepository monitorHistoryRepository,
            IncidentRepository incidentRepository,
            AnalyticsSummaryRepository analyticsSummaryRepository
    ) {
        this.monitorRepository = monitorRepository;
        this.monitorHistoryRepository = monitorHistoryRepository;
        this.incidentRepository = incidentRepository;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
    }

    @Transactional
    public void aggregateDailySummaries() {
        LocalDate summaryDate = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        Instant start = summaryDate.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant end = summaryDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<MonitorHistory> historyRecords = monitorHistoryRepository.findByCheckedAtBetweenOrderByCheckedAtAsc(
                start,
                end
        );
        Map<Long, List<MonitorHistory>> historyByMonitor = historyRecords.stream()
                .collect(Collectors.groupingBy(history -> history.getMonitor().getId()));

        List<Monitor> monitors = monitorRepository.findAll();
        monitors.forEach(monitor -> {
            List<MonitorHistory> history = historyByMonitor.getOrDefault(monitor.getId(), List.of());
            if (history.isEmpty()) {
                return;
            }
            List<Incident> incidents = incidentRepository.findByMonitorIdAndStartedAtBetween(
                    monitor.getId(),
                    start,
                    end
            );
            AnalyticsSummary summary = analyticsSummaryRepository.findByMonitorAndDate(monitor, summaryDate)
                    .orElseGet(() -> new AnalyticsSummary(monitor, summaryDate));
            fillSummary(summary, history, incidents);
            analyticsSummaryRepository.save(summary);
        });

        log.info("Completed daily analytics aggregation for summary date {}", summaryDate);
    }

    private void fillSummary(AnalyticsSummary summary, List<MonitorHistory> history, List<Incident> incidents) {
        long totalChecks = history.size();
        long successfulChecks = history.stream().filter(MonitorHistory::isSuccessful).count();
        long responseCount = history.stream().filter(h -> h.getResponseTimeMillis() != null).count();
        long sumResponseTimeMillis = history.stream()
                .map(MonitorHistory::getResponseTimeMillis)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();
        long incidentCount = incidents.stream().filter(incident -> incident.getStartedAt() != null).count();
        long totalDowntimeMillis = incidents.stream()
                .map(Incident::getDowntimeMillis)
                .filter(value -> value != null)
                .mapToLong(Long::longValue)
                .sum();
        long averageIncidentDurationMillis = incidentCount == 0 ? 0 : totalDowntimeMillis / incidentCount;
        long mtbfMillis = calculateMtbf(summary.getDate(), incidents);

        summary.setTotalChecks(totalChecks);
        summary.setSuccessfulChecks(successfulChecks);
        summary.setResponseCount(responseCount);
        summary.setSumResponseTimeMillis(sumResponseTimeMillis);
        summary.setAvailabilityPercent(totalChecks == 0 ? 0.0 : 100.0 * successfulChecks / totalChecks);
        summary.setAverageResponseTimeMillis(responseCount == 0 ? 0 : Math.round((double) sumResponseTimeMillis / responseCount));
        summary.setIncidentCount(incidentCount);
        summary.setTotalDowntimeMillis(totalDowntimeMillis);
        summary.setAverageIncidentDurationMillis(averageIncidentDurationMillis);
        summary.setMtbfMillis(mtbfMillis);
    }

    private long calculateMtbf(LocalDate summaryDate, List<Incident> incidents) {
        if (incidents.isEmpty()) {
            return 0;
        }
        long periodMillis = ChronoUnit.MILLIS.between(
                summaryDate.atStartOfDay(ZoneOffset.UTC),
                summaryDate.plusDays(1).atStartOfDay(ZoneOffset.UTC)
        );
        return periodMillis / incidents.size();
    }
}
