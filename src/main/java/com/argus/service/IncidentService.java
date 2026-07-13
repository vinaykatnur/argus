package com.argus.service;

import com.argus.entity.Incident;
import com.argus.enums.IncidentDateRange;
import com.argus.enums.IncidentStatus;
import com.argus.exception.ApiException;
import com.argus.repository.IncidentRepository;
import com.argus.response.IncidentTimelineItemResponse;
import com.argus.response.PageResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentService {

    private static final int MAX_PAGE_SIZE = 100;

    private final IncidentRepository incidentRepository;
    private final MonitorAccessService monitorAccessService;

    public IncidentService(IncidentRepository incidentRepository, MonitorAccessService monitorAccessService) {
        this.incidentRepository = incidentRepository;
        this.monitorAccessService = monitorAccessService;
    }

    @Transactional(readOnly = true)
    public PageResponse<IncidentTimelineItemResponse> list(
            Long ownerId,
            IncidentStatus status,
            Long monitorId,
            IncidentDateRange dateRange,
            Instant startDate,
            Instant endDate,
            int page,
            int size
    ) {
        if (monitorId != null) {
            monitorAccessService.findOwnedMonitor(ownerId, monitorId);
        }

        DateWindow window = resolveDateWindow(dateRange, startDate, endDate);
        Page<Incident> incidents = incidentRepository.findAll(
                ownedBy(ownerId)
                        .and(hasStatus(status))
                        .and(forMonitor(monitorId))
                        .and(inDateWindow(window)),
                PageRequest.of(
                        validatePage(page),
                        validateSize(size),
                        Sort.by(Sort.Order.desc("startedAt"))
                )
        );

        return PageResponse.from(incidents.map(IncidentTimelineItemResponse::from));
    }

    private DateWindow resolveDateWindow(IncidentDateRange dateRange, Instant startDate, Instant endDate) {
        if (dateRange == null) {
            return new DateWindow(startDate, endDate);
        }

        Instant now = Instant.now();
        return switch (dateRange) {
            case LAST_24_HOURS -> new DateWindow(now.minus(24, ChronoUnit.HOURS), now);
            case LAST_7_DAYS -> new DateWindow(now.minus(7, ChronoUnit.DAYS), now);
            case LAST_30_DAYS -> new DateWindow(now.minus(30, ChronoUnit.DAYS), now);
            case LAST_YEAR -> new DateWindow(now.minus(365, ChronoUnit.DAYS), now);
            case CUSTOM -> customDateWindow(startDate, endDate);
        };
    }

    private DateWindow customDateWindow(Instant startDate, Instant endDate) {
        if (startDate == null || endDate == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Custom date range requires startDate and endDate");
        }
        if (startDate.isAfter(endDate)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "startDate must be before endDate");
        }
        return new DateWindow(startDate, endDate);
    }

    private Specification<Incident> ownedBy(Long ownerId) {
        return (root, query, builder) -> builder.equal(root.get("monitor").get("owner").get("id"), ownerId);
    }

    private Specification<Incident> hasStatus(IncidentStatus status) {
        return (root, query, builder) -> status == null
                ? builder.conjunction()
                : builder.equal(root.get("status"), status);
    }

    private Specification<Incident> forMonitor(Long monitorId) {
        return (root, query, builder) -> monitorId == null
                ? builder.conjunction()
                : builder.equal(root.get("monitor").get("id"), monitorId);
    }

    private Specification<Incident> inDateWindow(DateWindow window) {
        return (root, query, builder) -> {
            if (window.startDate() == null && window.endDate() == null) {
                return builder.conjunction();
            }
            if (window.startDate() != null && window.endDate() != null) {
                return builder.between(root.get("startedAt"), window.startDate(), window.endDate());
            }
            if (window.startDate() != null) {
                return builder.greaterThanOrEqualTo(root.get("startedAt"), window.startDate());
            }
            return builder.lessThanOrEqualTo(root.get("startedAt"), window.endDate());
        };
    }

    private int validatePage(int page) {
        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page must be zero or greater");
        }
        return page;
    }

    private int validateSize(int size) {
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Size must be between 1 and " + MAX_PAGE_SIZE);
        }
        return size;
    }

    private record DateWindow(Instant startDate, Instant endDate) {
    }
}
