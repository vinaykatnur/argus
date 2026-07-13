package com.argus.service;

import com.argus.config.MonitorProperties;
import com.argus.entity.Monitor;
import com.argus.entity.User;
import com.argus.enums.MonitorStatus;
import com.argus.exception.ApiException;
import com.argus.exception.ResourceNotFoundException;
import com.argus.repository.AlertRepository;
import com.argus.repository.IncidentRepository;
import com.argus.repository.MonitorRepository;
import com.argus.repository.UserRepository;
import com.argus.request.CreateMonitorRequest;
import com.argus.request.UpdateMonitorRequest;
import com.argus.response.MessageResponse;
import com.argus.response.MonitorResponse;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class MonitorManagementService {

    private static final Logger log = LoggerFactory.getLogger(MonitorManagementService.class);

    private final MonitorRepository monitorRepository;
    private final IncidentRepository incidentRepository;
    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final MonitorUrlNormalizer urlNormalizer;
    private final MonitorProperties monitorProperties;
    private final MonitorAccessService monitorAccessService;
    private final HealthCheckExecutor healthCheckExecutor;
    private final MonitoringResultService monitoringResultService;

    public MonitorManagementService(
            MonitorRepository monitorRepository,
            IncidentRepository incidentRepository,
            AlertRepository alertRepository,
            UserRepository userRepository,
            MonitorUrlNormalizer urlNormalizer,
            MonitorProperties monitorProperties,
            MonitorAccessService monitorAccessService,
            HealthCheckExecutor healthCheckExecutor,
            MonitoringResultService monitoringResultService
    ) {
        this.monitorRepository = monitorRepository;
        this.incidentRepository = incidentRepository;
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
        this.urlNormalizer = urlNormalizer;
        this.monitorProperties = monitorProperties;
        this.monitorAccessService = monitorAccessService;
        this.healthCheckExecutor = healthCheckExecutor;
        this.monitoringResultService = monitoringResultService;
    }

    @Transactional
    public MonitorResponse create(Long ownerId, CreateMonitorRequest request) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        validateInterval(request.intervalSeconds());
        validateFailureThreshold(request.failureThreshold());

        Monitor monitor = new Monitor(
                owner,
                urlNormalizer.normalize(request.url()),
                normalizeDisplayName(request.displayName()),
                request.intervalSeconds(),
                request.failureThreshold()
        );
        monitor.setEmailDowntimeNotificationsEnabled(resolveNotificationPreference(
                request.emailDowntimeNotificationsEnabled()
        ));
        monitor.setEmailRecoveryNotificationsEnabled(resolveNotificationPreference(
                request.emailRecoveryNotificationsEnabled()
        ));

        Monitor savedMonitor = monitorRepository.save(monitor);
        log.info("Monitor created: id={}, ownerId={}", savedMonitor.getId(), ownerId);
        return MonitorResponse.from(savedMonitor);
    }

    @Transactional
    public MonitorResponse update(Long ownerId, Long monitorId, UpdateMonitorRequest request) {
        Monitor monitor = findOwnedMonitor(ownerId, monitorId);

        validateInterval(request.intervalSeconds());
        validateFailureThreshold(request.failureThreshold());

        monitor.setUrl(urlNormalizer.normalize(request.url()));
        monitor.setDisplayName(normalizeDisplayName(request.displayName()));
        monitor.setIntervalSeconds(request.intervalSeconds());
        monitor.setFailureThreshold(request.failureThreshold());
        monitor.setEmailDowntimeNotificationsEnabled(resolveNotificationPreference(
                request.emailDowntimeNotificationsEnabled()
        ));
        monitor.setEmailRecoveryNotificationsEnabled(resolveNotificationPreference(
                request.emailRecoveryNotificationsEnabled()
        ));
        if (monitor.isEligibleForMonitoring()) {
            monitor.setNextCheckAt(Instant.now());
        }

        log.info("Monitor updated: id={}, ownerId={}", monitorId, ownerId);
        return MonitorResponse.from(monitor);
    }

    @Transactional
    public MessageResponse delete(Long ownerId, Long monitorId) {
        Monitor monitor = findOwnedMonitor(ownerId, monitorId);
        alertRepository.deleteByMonitor(monitor);
        incidentRepository.deleteByMonitor(monitor);
        monitorRepository.delete(monitor);
        log.info("Monitor deleted: id={}, ownerId={}", monitorId, ownerId);
        return new MessageResponse("Monitor deleted successfully");
    }

    @Transactional
    public MonitorResponse pause(Long ownerId, Long monitorId) {
        Monitor monitor = findOwnedMonitor(ownerId, monitorId);
        monitor.setStatus(MonitorStatus.PAUSED);
        monitor.setNextCheckAt(null);
        monitor.setCheckInProgress(false);
        log.info("Monitor paused: id={}, ownerId={}", monitorId, ownerId);
        return MonitorResponse.from(monitor);
    }

    @Transactional
    public MonitorResponse resume(Long ownerId, Long monitorId) {
        Monitor monitor = findOwnedMonitor(ownerId, monitorId);
        monitor.setActive(true);
        monitor.setStatus(MonitorStatus.HEALTHY);
        monitor.setNextCheckAt(Instant.now());
        log.info("Monitor resumed: id={}, ownerId={}", monitorId, ownerId);
        return MonitorResponse.from(monitor);
    }

    @Transactional(readOnly = true)
    public MonitorResponse get(Long ownerId, Long monitorId) {
        return MonitorResponse.from(findOwnedMonitor(ownerId, monitorId));
    }

    @Transactional(readOnly = true)
    public List<MonitorResponse> list(Long ownerId) {
        return monitorRepository.findByOwnerIdOrderByCreatedAtDesc(ownerId)
                .stream()
                .map(MonitorResponse::from)
                .toList();
    }

    @Transactional
    public MonitorResponse pin(Long ownerId, Long monitorId, Integer requestedPosition) {
        Monitor monitor = findOwnedMonitor(ownerId, monitorId);
        monitor.setPinned(true);
        monitor.setPinnedPosition(resolvePinnedPosition(ownerId, requestedPosition));
        log.info("Monitor pinned: id={}, ownerId={}", monitorId, ownerId);
        return MonitorResponse.from(monitor);
    }

    @Transactional
    public MonitorResponse unpin(Long ownerId, Long monitorId) {
        Monitor monitor = findOwnedMonitor(ownerId, monitorId);
        monitor.setPinned(false);
        monitor.setPinnedPosition(null);
        log.info("Monitor unpinned: id={}, ownerId={}", monitorId, ownerId);
        return MonitorResponse.from(monitor);
    }

    @Transactional
    public MonitorResponse runManualHealthCheck(Long ownerId, Long monitorId) {
        Monitor monitor = findOwnedMonitor(ownerId, monitorId);
        if (!monitor.isEligibleForMonitoring()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Paused monitors cannot run manual health checks");
        }
        if (monitor.isCheckInProgress()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Monitor health check is already in progress");
        }

        Instant nextScheduledCheck = monitor.getNextCheckAt();
        HealthCheckResult result = healthCheckExecutor.check(monitor.getUrl());
        monitoringResultService.processResult(monitor.getId(), result);
        monitor.setNextCheckAt(nextScheduledCheck);
        log.info("Manual health check completed: id={}, ownerId={}", monitorId, ownerId);
        return MonitorResponse.from(monitor);
    }

    private Monitor findOwnedMonitor(Long ownerId, Long monitorId) {
        return monitorAccessService.findOwnedMonitor(ownerId, monitorId);
    }

    private int resolvePinnedPosition(Long ownerId, Integer requestedPosition) {
        if (requestedPosition != null) {
            return requestedPosition;
        }
        return monitorRepository.findMaxPinnedPositionByOwnerId(ownerId) + 1;
    }

    private void validateInterval(int intervalSeconds) {
        if (intervalSeconds < monitorProperties.getMinimumIntervalSeconds()
                || intervalSeconds > monitorProperties.getMaximumIntervalSeconds()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Monitor interval must be between %d and %d seconds".formatted(
                            monitorProperties.getMinimumIntervalSeconds(),
                            monitorProperties.getMaximumIntervalSeconds()
                    )
            );
        }
    }

    private void validateFailureThreshold(int failureThreshold) {
        if (failureThreshold < monitorProperties.getMinimumFailureThreshold()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Failure threshold must be at least %d".formatted(
                            monitorProperties.getMinimumFailureThreshold()
                    )
            );
        }
    }

    private String normalizeDisplayName(String displayName) {
        return StringUtils.hasText(displayName) ? displayName.trim() : null;
    }

    private boolean resolveNotificationPreference(Boolean value) {
        return value == null || value;
    }
}
