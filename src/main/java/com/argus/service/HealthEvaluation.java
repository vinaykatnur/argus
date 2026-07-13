package com.argus.service;

import com.argus.enums.AlertType;
import com.argus.enums.MonitorStatus;

public record HealthEvaluation(
        boolean successful,
        MonitorStatus monitorStatus,
        AlertType alertType,
        String alertMessage
) {

    public boolean hasAlert() {
        return alertType != null;
    }
}
