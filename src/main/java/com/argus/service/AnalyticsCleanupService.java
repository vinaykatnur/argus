package com.argus.service;

import com.argus.config.AnalyticsProperties;
import com.argus.repository.AnalyticsReportRequestRepository;
import com.argus.repository.AnalyticsSummaryRepository;
import com.argus.repository.MonitorHistoryRepository;
import java.time.Instant;
import java.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AnalyticsCleanupService {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsCleanupService.class);

    private final MonitorHistoryRepository monitorHistoryRepository;
    private final AnalyticsSummaryRepository analyticsSummaryRepository;
    private final AnalyticsReportRequestRepository analyticsReportRequestRepository;
    private final AnalyticsProperties analyticsProperties;

    public AnalyticsCleanupService(
            MonitorHistoryRepository monitorHistoryRepository,
            AnalyticsSummaryRepository analyticsSummaryRepository,
            AnalyticsReportRequestRepository analyticsReportRequestRepository,
            AnalyticsProperties analyticsProperties
    ) {
        this.monitorHistoryRepository = monitorHistoryRepository;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
        this.analyticsReportRequestRepository = analyticsReportRequestRepository;
        this.analyticsProperties = analyticsProperties;
    }

    @Scheduled(fixedDelayString = "${argus.analytics.cleanup-fixed-delay-millis:3600000}")
    @Transactional
    public void cleanupExpiredAnalyticsData() {
        Instant rawCutoff = Instant.now().minus(analyticsProperties.getRawDataRetentionDays(), java.time.temporal.ChronoUnit.DAYS);
        LocalDate summaryCutoff = LocalDate.now().minusDays(analyticsProperties.getSummaryRetentionDays());
        Instant reportCutoff = Instant.now().minus(analyticsProperties.getReportRetentionDays(), java.time.temporal.ChronoUnit.DAYS);

        monitorHistoryRepository.deleteByCheckedAtBefore(rawCutoff);
        analyticsSummaryRepository.deleteByDateBefore(summaryCutoff);
        analyticsReportRequestRepository.deleteByRequestedAtBefore(reportCutoff);

        log.info("Analytics cleanup completed: raw cutoff={}, summary cutoff={}, report cutoff={}", rawCutoff, summaryCutoff, reportCutoff);
    }
}
