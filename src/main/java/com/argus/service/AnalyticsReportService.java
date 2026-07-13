package com.argus.service;

import com.argus.config.AnalyticsProperties;
import com.argus.entity.AnalyticsReportRequest;
import com.argus.entity.AnalyticsSummary;
import com.argus.enums.AnalyticsReportStatus;
import com.argus.enums.IncidentDateRange;
import com.argus.exception.ApiException;
import com.argus.exception.ResourceNotFoundException;
import com.argus.repository.AnalyticsReportRequestRepository;
import com.argus.repository.AnalyticsSummaryRepository;
import com.argus.repository.UserRepository;
import com.argus.response.AnalyticsReportResponse;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsReportService {

    private final AnalyticsReportRequestRepository analyticsReportRequestRepository;
    private final AnalyticsSummaryRepository analyticsSummaryRepository;
    private final UserRepository userRepository;
    private final AnalyticsProperties analyticsProperties;

    public AnalyticsReportService(
            AnalyticsReportRequestRepository analyticsReportRequestRepository,
            AnalyticsSummaryRepository analyticsSummaryRepository,
            UserRepository userRepository,
            AnalyticsProperties analyticsProperties
    ) {
        this.analyticsReportRequestRepository = analyticsReportRequestRepository;
        this.analyticsSummaryRepository = analyticsSummaryRepository;
        this.userRepository = userRepository;
        this.analyticsProperties = analyticsProperties;
    }

    @Transactional
    public AnalyticsReportResponse requestReport(Long ownerId, IncidentDateRange dateRange, Instant startDate, Instant endDate) {
        Objects.requireNonNull(ownerId, "ownerId is required");
        if (dateRange == IncidentDateRange.CUSTOM) {
            validateCustomDates(startDate, endDate);
        }

        var owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        AnalyticsReportRequest request = new AnalyticsReportRequest(owner, dateRange, startDate, endDate);
        if (durationExceedsLargeReportThreshold(dateRange, startDate, endDate)) {
            request.setStatus(AnalyticsReportStatus.PENDING);
            request.setWarningMessage("Large report generation is queued and will be available when ready.");
            analyticsReportRequestRepository.save(request);
            return toResponse(request);
        }

        request.setStatus(AnalyticsReportStatus.READY);
        request.setResultJson(generateReport(ownerId, dateRange, startDate, endDate));
        analyticsReportRequestRepository.save(request);
        return toResponse(request);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsReportResponse> listReports(Long ownerId) {
        return analyticsReportRequestRepository.findByOwner_IdOrderByRequestedAtDesc(ownerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AnalyticsReportResponse getReport(Long ownerId, Long requestId) {
        return analyticsReportRequestRepository.findByIdAndOwner_Id(requestId, ownerId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics report request not found"));
    }

    @Transactional(readOnly = true)
    public String getReportResult(Long ownerId, Long requestId) {
        AnalyticsReportRequest request = analyticsReportRequestRepository.findByIdAndOwner_Id(requestId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Analytics report request not found"));

        if (request.getStatus() != AnalyticsReportStatus.READY) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Report is not ready for download");
        }
        return Objects.requireNonNullElse(request.getResultJson(), "{}");
    }

    @Scheduled(fixedDelayString = "${argus.analytics.report-processing-fixed-delay-millis:60000}")
    @Transactional
    public void processPendingReports() {
        List<AnalyticsReportRequest> pending = analyticsReportRequestRepository.findByStatus(AnalyticsReportStatus.PENDING);
        pending.forEach(request -> {
            try {
                Long ownerId = request.getOwner().getId();
                request.setResultJson(generateReport(ownerId, request.getDateRange(), request.getCustomStartDate(), request.getCustomEndDate()));
                request.setStatus(AnalyticsReportStatus.READY);
                analyticsReportRequestRepository.save(request);
            } catch (Exception ex) {
                request.setStatus(AnalyticsReportStatus.FAILED);
                request.setWarningMessage("Failed to generate report: " + ex.getMessage());
                analyticsReportRequestRepository.save(request);
            }
        });
    }

    private boolean durationExceedsLargeReportThreshold(IncidentDateRange dateRange, Instant startDate, Instant endDate) {
        long days = switch (dateRange) {
            case LAST_24_HOURS -> 1;
            case LAST_7_DAYS -> 7;
            case LAST_30_DAYS -> 30;
            case LAST_YEAR -> 365;
            case CUSTOM -> ChronoUnit.DAYS.between(startDate, endDate);
        };
        return days > analyticsProperties.getLargeReportThresholdDays();
    }

    private String generateReport(Long ownerId, IncidentDateRange dateRange, Instant startDate, Instant endDate) {
        LocalDateRange dates = resolveDateRange(dateRange, startDate, endDate);
        List<AnalyticsSummary> summaries = analyticsSummaryRepository.findByOwnerIdAndDateBetweenOrderByDateAsc(ownerId, dates.start, dates.end);
        return buildReportJson(summaries);
    }

    private String buildReportJson(List<AnalyticsSummary> summaries) {
        long summaryCount = summaries.size();
        double averageAvailability = summaries.stream()
                .mapToDouble(AnalyticsSummary::getAvailabilityPercent)
                .average()
                .orElse(0.0);
        return "{\"summaryCount\":" + summaryCount
                + ",\"averageAvailability\":" + String.format("%.2f", averageAvailability)
                + "}";
    }

    private LocalDateRange resolveDateRange(IncidentDateRange dateRange, Instant startDate, Instant endDate) {
        return switch (dateRange) {
            case LAST_24_HOURS -> new LocalDateRange(LocalDate.now(ZoneOffset.UTC).minusDays(1), LocalDate.now(ZoneOffset.UTC));
            case LAST_7_DAYS -> new LocalDateRange(LocalDate.now(ZoneOffset.UTC).minusDays(7), LocalDate.now(ZoneOffset.UTC));
            case LAST_30_DAYS -> new LocalDateRange(LocalDate.now(ZoneOffset.UTC).minusDays(30), LocalDate.now(ZoneOffset.UTC));
            case LAST_YEAR -> new LocalDateRange(LocalDate.now(ZoneOffset.UTC).minusDays(365), LocalDate.now(ZoneOffset.UTC));
            case CUSTOM -> new LocalDateRange(startDate.atZone(ZoneOffset.UTC).toLocalDate(), endDate.atZone(ZoneOffset.UTC).toLocalDate());
        };
    }

    private void validateCustomDates(Instant startDate, Instant endDate) {
        if (startDate == null || endDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Custom date range requires startDate and endDate");
        }
        if (startDate.isAfter(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }
    }

    private AnalyticsReportResponse toResponse(AnalyticsReportRequest request) {
        return new AnalyticsReportResponse(
                request.getId(),
                request.getStatus().name(),
                request.getWarningMessage(),
                request.getRequestedAt(),
                request.getCompletedAt(),
                request.getResultJson()
        );
    }

    private record LocalDateRange(LocalDate start, LocalDate end) {
    }
}
