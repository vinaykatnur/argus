package com.argus.service;

import com.argus.entity.Alert;
import com.argus.entity.Monitor;
import com.argus.repository.AlertRepository;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    public void createAlert(Monitor monitor, HealthEvaluation evaluation, HealthCheckResult result) {
        if (!evaluation.hasAlert()) {
            return;
        }

        Alert alert = new Alert(
                monitor,
                evaluation.alertType(),
                evaluation.alertMessage(),
                result.httpStatusCode(),
                result.responseTimeMillis()
        );
        alertRepository.save(alert);
    }
}
