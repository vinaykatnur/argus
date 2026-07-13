package com.argus.service;

import com.argus.entity.Monitor;
import com.argus.enums.IncidentStatus;
import com.argus.enums.MonitorStatus;
import com.argus.repository.IncidentRepository;
import com.argus.repository.MonitorRepository;
import com.argus.response.DashboardMessagesResponse;
import com.argus.response.DashboardResponse;
import com.argus.response.DashboardSummaryResponse;
import com.argus.response.IncidentTimelineItemResponse;
import com.argus.response.MonitorListItemResponse;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final int DASHBOARD_SECTION_LIMIT = 5;

    private final MonitorRepository monitorRepository;
    private final IncidentRepository incidentRepository;

    public DashboardService(MonitorRepository monitorRepository, IncidentRepository incidentRepository) {
        this.monitorRepository = monitorRepository;
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(Long ownerId) {
        DashboardSummaryResponse summary = summary(ownerId);
        List<MonitorListItemResponse> pinnedMonitors = monitorItems(
                ownerId,
                monitorRepository.findTop5ByOwnerIdAndPinnedTrueOrderByPinnedPositionAscIdAsc(ownerId)
        );
        List<MonitorListItemResponse> needsAttention = needsAttention(ownerId);
        List<IncidentTimelineItemResponse> recentIncidents = incidentRepository
                .findRecentByOwnerId(ownerId, PageRequest.of(0, DASHBOARD_SECTION_LIMIT))
                .stream()
                .map(IncidentTimelineItemResponse::from)
                .toList();

        return new DashboardResponse(
                summary,
                pinnedMonitors,
                needsAttention,
                recentIncidents,
                DashboardMessagesResponse.defaults()
        );
    }

    private DashboardSummaryResponse summary(Long ownerId) {
        Double averageResponseTime = monitorRepository.findAverageResponseTimeMillisByOwnerId(ownerId);
        return new DashboardSummaryResponse(
                monitorRepository.countByOwnerId(ownerId),
                monitorRepository.countByOwnerIdAndStatus(ownerId, MonitorStatus.HEALTHY),
                monitorRepository.countByOwnerIdAndStatus(ownerId, MonitorStatus.DOWN),
                monitorRepository.countByOwnerIdAndStatus(ownerId, MonitorStatus.SLOW),
                monitorRepository.countByOwnerIdAndStatus(ownerId, MonitorStatus.PAUSED),
                incidentRepository.countByMonitor_Owner_IdAndStatus(ownerId, IncidentStatus.ACTIVE),
                averageResponseTime == null ? null : Math.round(averageResponseTime)
        );
    }

    private List<MonitorListItemResponse> needsAttention(Long ownerId) {
        List<Monitor> candidates = monitorRepository.findNeedsAttentionCandidates(
                ownerId,
                List.of(MonitorStatus.DOWN, MonitorStatus.SLOW),
                PageRequest.of(0, 25)
        );
        Set<Long> activeMonitorIds = findActiveMonitorIds(ownerId, candidates);
        return candidates.stream()
                .sorted(Comparator
                        .comparingInt((Monitor monitor) -> severity(monitor, activeMonitorIds))
                        .thenComparing(Monitor::getLastCheckedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(DASHBOARD_SECTION_LIMIT)
                .map(monitor -> MonitorListItemResponse.from(monitor, activeMonitorIds.contains(monitor.getId())))
                .toList();
    }

    private List<MonitorListItemResponse> monitorItems(Long ownerId, List<Monitor> monitors) {
        Set<Long> activeMonitorIds = findActiveMonitorIds(ownerId, monitors);
        return monitors.stream()
                .map(monitor -> MonitorListItemResponse.from(monitor, activeMonitorIds.contains(monitor.getId())))
                .toList();
    }

    private Set<Long> findActiveMonitorIds(Long ownerId, List<Monitor> monitors) {
        List<Long> monitorIds = monitors.stream().map(Monitor::getId).toList();
        if (monitorIds.isEmpty()) {
            return Set.of();
        }
        return new HashSet<>(incidentRepository.findActiveMonitorIds(ownerId, monitorIds));
    }

    private int severity(Monitor monitor, Set<Long> activeMonitorIds) {
        if (activeMonitorIds.contains(monitor.getId())) {
            return 0;
        }
        if (monitor.getStatus() == MonitorStatus.DOWN) {
            return 1;
        }
        if (monitor.getStatus() == MonitorStatus.SLOW) {
            return 2;
        }
        return 3;
    }
}
