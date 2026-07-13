package com.argus.service;

import com.argus.entity.Monitor;
import com.argus.repository.MonitorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MonitoringWorker {

    private static final Logger log = LoggerFactory.getLogger(MonitoringWorker.class);

    private final MonitorRepository monitorRepository;
    private final HealthCheckExecutor healthCheckExecutor;
    private final MonitoringResultService monitoringResultService;

    public MonitoringWorker(
            MonitorRepository monitorRepository,
            HealthCheckExecutor healthCheckExecutor,
            MonitoringResultService monitoringResultService
    ) {
        this.monitorRepository = monitorRepository;
        this.healthCheckExecutor = healthCheckExecutor;
        this.monitoringResultService = monitoringResultService;
    }

    public void process(Long monitorId) {
        monitorRepository.findById(monitorId)
                .ifPresentOrElse(this::runHealthCheck, () -> log.debug("Skipped missing monitor: id={}", monitorId));
    }

    private void runHealthCheck(Monitor monitor) {
        if (!monitor.isEligibleForMonitoring()) {
            monitoringResultService.releaseClaim(monitor.getId());
            return;
        }

        log.info("Health check started: monitorId={}", monitor.getId());
        try {
            HealthCheckResult result = healthCheckExecutor.check(monitor.getUrl());
            monitoringResultService.processResult(monitor.getId(), result);
            log.info("Health check completed: monitorId={}", monitor.getId());
        } catch (RuntimeException exception) {
            monitoringResultService.releaseClaim(monitor.getId());
            throw exception;
        }
    }
}
