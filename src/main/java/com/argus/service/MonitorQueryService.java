package com.argus.service;

import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import com.argus.enums.IncidentStatus;
import com.argus.enums.MonitorSortOption;
import com.argus.enums.MonitorStatus;
import com.argus.exception.ApiException;
import com.argus.repository.AlertRepository;
import com.argus.repository.IncidentRepository;
import com.argus.repository.MonitorRepository;
import com.argus.response.CurrentIncidentResponse;
import com.argus.response.IncidentTimelineItemResponse;
import com.argus.response.MonitorConfigurationResponse;
import com.argus.response.MonitorDetailsResponse;
import com.argus.response.MonitorListItemResponse;
import com.argus.response.NotificationPreferencesResponse;
import com.argus.response.PageResponse;
import com.argus.response.ResponseTimeSummaryResponse;
import jakarta.persistence.criteria.Subquery;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MonitorQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private final MonitorRepository monitorRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final MonitorAccessService monitorAccessService;
    private final NotificationPreferenceService notificationPreferenceService;

    public MonitorQueryService(
            MonitorRepository monitorRepository,
            IncidentRepository incidentRepository,
            AlertRepository alertRepository,
            MonitorAccessService monitorAccessService,
            NotificationPreferenceService notificationPreferenceService
    ) {
        this.monitorRepository = monitorRepository;
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.monitorAccessService = monitorAccessService;
        this.notificationPreferenceService = notificationPreferenceService;
    }

    @Transactional(readOnly = true)
    public PageResponse<MonitorListItemResponse> list(
            Long ownerId,
            String search,
            List<MonitorStatus> statuses,
            Boolean activeIncident,
            MonitorSortOption sort,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(validatePage(page), validateSize(size), resolveSort(sort));
        Page<Monitor> monitors = monitorRepository.findAll(
                ownedBy(ownerId)
                        .and(matchesSearch(search))
                        .and(hasStatuses(statuses))
                        .and(hasActiveIncident(ownerId, activeIncident)),
                pageable
        );
        Set<Long> activeMonitorIds = findActiveMonitorIds(ownerId, monitors.getContent());
        return PageResponse.from(monitors.map(monitor -> MonitorListItemResponse.from(
                monitor,
                activeMonitorIds.contains(monitor.getId())
        )));
    }

    @Transactional(readOnly = true)
    public MonitorDetailsResponse getDetails(Long ownerId, Long monitorId) {
        Monitor monitor = monitorAccessService.findOwnedMonitor(ownerId, monitorId);
        CurrentIncidentResponse currentIncident = resolveCurrentIncident(ownerId, monitor);
        List<IncidentTimelineItemResponse> recentIncidents = incidentRepository
                .findTop5ByMonitor_IdAndMonitor_Owner_IdOrderByStartedAtDesc(monitorId, ownerId)
                .stream()
                .map(IncidentTimelineItemResponse::from)
                .toList();

        return new MonitorDetailsResponse(
                monitor.getId(),
                monitor.getStatus(),
                responseTimes(ownerId, monitor),
                new MonitorConfigurationResponse(
                        monitor.getUrl(),
                        monitor.getDisplayName(),
                        monitor.getIntervalSeconds(),
                        monitor.getFailureThreshold()
                ),
                NotificationPreferencesResponse.from(
                        notificationPreferenceService.findExistingOrDefault(monitor)
                ),
                monitor.isPaused(),
                monitor.isPinned(),
                monitor.getPinnedPosition(),
                monitor.getLastCheckedAt(),
                currentIncident,
                recentIncidents
        );
    }

    private Set<Long> findActiveMonitorIds(Long ownerId, List<Monitor> monitors) {
        List<Long> monitorIds = monitors.stream().map(Monitor::getId).toList();
        if (monitorIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(incidentRepository.findActiveMonitorIds(ownerId, monitorIds));
    }

    private CurrentIncidentResponse resolveCurrentIncident(Long ownerId, Monitor monitor) {
        if (monitor.getStatus() != MonitorStatus.DOWN) {
            return null;
        }

        return incidentRepository.findFirstByMonitor_IdAndMonitor_Owner_IdAndStatusOrderByStartedAtDesc(
                        monitor.getId(),
                        ownerId,
                        IncidentStatus.ACTIVE
                )
                .map(incident -> new CurrentIncidentResponse(
                        incident.getId(),
                        incident.getStartedAt(),
                        "Monitor failed recent health checks",
                        monitor.getFailureThreshold(),
                        monitor.getConsecutiveFailureCount(),
                        monitor.getLastSuccessfulCheckAt()
                ))
                .orElse(null);
    }

    private ResponseTimeSummaryResponse responseTimes(Long ownerId, Monitor monitor) {
        Long current = monitor.getLastResponseTimeMillis();
        Long average = rounded(alertRepository.findAverageResponseTimeMillis(monitor.getId(), ownerId), current);
        Long fastest = fallback(alertRepository.findFastestResponseTimeMillis(monitor.getId(), ownerId), current);
        Long slowest = fallback(alertRepository.findSlowestResponseTimeMillis(monitor.getId(), ownerId), current);
        return new ResponseTimeSummaryResponse(current, average, fastest, slowest);
    }

    private Long rounded(Double value, Long fallback) {
        return value == null ? fallback : Math.round(value);
    }

    private Long fallback(Long value, Long fallback) {
        return value == null ? fallback : value;
    }

    private Specification<Monitor> ownedBy(Long ownerId) {
        return (root, query, builder) -> builder.equal(root.get("owner").get("id"), ownerId);
    }

    private Specification<Monitor> matchesSearch(String search) {
        return (root, query, builder) -> {
            if (!StringUtils.hasText(search)) {
                return builder.conjunction();
            }
            String pattern = "%" + search.trim().toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("displayName")), pattern),
                    builder.like(builder.lower(root.get("url")), pattern)
            );
        };
    }

    private Specification<Monitor> hasStatuses(List<MonitorStatus> statuses) {
        return (root, query, builder) -> {
            if (statuses == null || statuses.isEmpty()) {
                return builder.conjunction();
            }
            return root.get("status").in(statuses);
        };
    }

    private Specification<Monitor> hasActiveIncident(Long ownerId, Boolean activeIncident) {
        return (root, query, builder) -> {
            if (activeIncident == null) {
                return builder.conjunction();
            }

            Subquery<Long> subquery = query.subquery(Long.class);
            var incident = subquery.from(Incident.class);
            subquery.select(incident.get("id"))
                    .where(
                            builder.equal(incident.get("monitor"), root),
                            builder.equal(incident.get("monitor").get("owner").get("id"), ownerId),
                            builder.equal(incident.get("status"), IncidentStatus.ACTIVE)
                    );
            return activeIncident ? builder.exists(subquery) : builder.not(builder.exists(subquery));
        };
    }

    private Sort resolveSort(MonitorSortOption sort) {
        MonitorSortOption resolved = sort == null ? MonitorSortOption.LAST_CHECKED : sort;
        return switch (resolved) {
            case NAME_ASC -> Sort.by(Sort.Order.asc("displayName").ignoreCase(), Sort.Order.asc("url").ignoreCase());
            case NAME_DESC -> Sort.by(Sort.Order.desc("displayName").ignoreCase(), Sort.Order.desc("url").ignoreCase());
            case STATUS -> Sort.by(Sort.Order.asc("status"), Sort.Order.asc("displayName").ignoreCase());
            case RESPONSE_TIME -> Sort.by(Sort.Order.desc("lastResponseTimeMillis").nullsLast());
            case LAST_CHECKED -> Sort.by(Sort.Order.desc("lastCheckedAt").nullsLast());
            case MOST_INCIDENTS -> Sort.by(Sort.Order.desc("incidentCount"), Sort.Order.asc("displayName").ignoreCase());
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
}
