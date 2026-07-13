package com.argus.request;

import jakarta.validation.constraints.Positive;
import java.time.LocalTime;

public record NotificationPreferencesRequest(
        Boolean emailEnabled,
        Boolean downAlertsEnabled,
        Boolean recoveryAlertsEnabled,
        Boolean slowAlertsEnabled,
        Boolean reminderNotificationsEnabled,

        @Positive
        Integer reminderFrequencyMinutes,

        Boolean quietHoursEnabled,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd,
        Boolean criticalMonitor
) {
}
