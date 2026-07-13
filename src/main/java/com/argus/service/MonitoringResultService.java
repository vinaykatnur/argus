package com.argus.service;

import com.argus.entity.Monitor;
import com.argus.enums.MonitorStatus;
import com.argus.repository.MonitorRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MonitoringResultService {

    private final MonitorRepository monitorRepository;
    private final HealthEvaluationService healthEvaluationService;
    private final AlertService alertService;
    private final IncidentCoordinationService incidentCoordinationService;

    public MonitoringResultService(
            MonitorRepository monitorRepository,
            HealthEvaluationService healthEvaluationService,
            AlertService alertService,
            IncidentCoordinationService incidentCoordinationService
    ) {
        this.monitorRepository = monitorRepository;
        this.healthEvaluationService = healthEvaluationService;
        this.alertService = alertService;
        this.incidentCoordinationService = incidentCoordinationService;
    }

    @Transactional
    public void processResult(Long monitorId, HealthCheckResult result) {
        monitorRepository.findById(monitorId).ifPresent(monitor -> applyResult(monitor, result));
    }

    @Transactional
    public void releaseClaim(Long monitorId) {
        monitorRepository.findById(monitorId).ifPresent(monitor -> monitor.setCheckInProgress(false));
    }

    private void applyResult(Monitor monitor, HealthCheckResult result) {
        if (!monitor.isEligibleForMonitoring()) {
            monitor.setCheckInProgress(false);
            return;
        }

        Instant checkedAt = Instant.now();
        HealthEvaluation evaluation = healthEvaluationService.evaluate(result);

        monitor.setLastCheckedAt(checkedAt);
        monitor.setLastResponseTimeMillis(result.responseTimeMillis());

        if (evaluation.successful()) {
            handleSuccess(monitor, evaluation, result, checkedAt);
        } else {
            handleFailure(monitor, evaluation, result, checkedAt);
        }

        monitor.scheduleNextCheck(checkedAt);
        monitor.setCheckInProgress(false);
    }

    private void handleSuccess(
            Monitor monitor,
            HealthEvaluation evaluation,
            HealthCheckResult result,
            Instant checkedAt
    ) {
        monitor.setConsecutiveFailureCount(0);
        monitor.setLastSuccessfulCheckAt(checkedAt);
        monitor.setStatus(evaluation.monitorStatus());
        alertService.createAlert(monitor, evaluation, result);
        incidentCoordinationService.resolveActiveIncidentIfPresent(monitor, checkedAt);
    }

    private void handleFailure(
            Monitor monitor,
            HealthEvaluation evaluation,
            HealthCheckResult result,
            Instant checkedAt
    ) {
        int failureCount = monitor.getConsecutiveFailureCount() + 1;
        monitor.setConsecutiveFailureCount(failureCount);

        if (failureCount >= monitor.getFailureThreshold()) {
            monitor.setStatus(MonitorStatus.DOWN);
            incidentCoordinationService.createIncidentIfAbsent(monitor, checkedAt);
            return;
        }

        alertService.createAlert(monitor, evaluation, result);
    }
}
