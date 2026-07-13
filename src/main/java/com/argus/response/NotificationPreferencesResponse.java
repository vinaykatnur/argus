package com.argus.response;

import com.argus.entity.NotificationPreference;
import java.time.LocalTime;

public record NotificationPreferencesResponse(
        boolean emailEnabled,
        boolean downAlertsEnabled,
        boolean recoveryAlertsEnabled,
        boolean slowAlertsEnabled,
        boolean reminderNotificationsEnabled,
        Integer reminderFrequencyMinutes,
        boolean quietHoursEnabled,
        LocalTime quietHoursStart,
        LocalTime quietHoursEnd,
        boolean criticalMonitor,
        boolean emailDowntimeNotificationsEnabled,
        boolean emailRecoveryNotificationsEnabled
) {

    public static NotificationPreferencesResponse from(NotificationPreference preference) {
        return new NotificationPreferencesResponse(
                preference.isEmailEnabled(),
                preference.isDownAlertsEnabled(),
                preference.isRecoveryAlertsEnabled(),
                preference.isSlowAlertsEnabled(),
                preference.isReminderNotificationsEnabled(),
                preference.getReminderFrequencyMinutes(),
                preference.isQuietHoursEnabled(),
                preference.getQuietHoursStart(),
                preference.getQuietHoursEnd(),
                preference.isCriticalMonitor(),
                preference.isEmailEnabled() && preference.isDownAlertsEnabled(),
                preference.isEmailEnabled() && preference.isRecoveryAlertsEnabled()
        );
    }
}
