package com.argus.service;

import com.argus.config.MonitorProperties;
import com.argus.enums.AlertType;
import com.argus.enums.MonitorStatus;
import org.springframework.stereotype.Service;

@Service
public class HealthEvaluationService {

    private final MonitorProperties monitorProperties;

    public HealthEvaluationService(MonitorProperties monitorProperties) {
        this.monitorProperties = monitorProperties;
    }

    public HealthEvaluation evaluate(HealthCheckResult result) {
        if (!result.successful()) {
            return new HealthEvaluation(
                    false,
                    MonitorStatus.DOWN,
                    AlertType.TEMPORARY_FAILURE,
                    failureMessage(result)
            );
        }

        if (result.responseTimeMillis() != null
                && result.responseTimeMillis() > monitorProperties.getSlowResponseThresholdMillis()) {
            return new HealthEvaluation(
                    true,
                    MonitorStatus.SLOW,
                    AlertType.SLOW_RESPONSE,
                    "Successful response was slower than the configured threshold"
            );
        }

        return new HealthEvaluation(true, MonitorStatus.HEALTHY, null, null);
    }

    private String failureMessage(HealthCheckResult result) {
        if (result.httpStatusCode() != null) {
            return "Received non-success HTTP status " + result.httpStatusCode();
        }
        return "Health check failed: " + result.failureReason();
    }
}
