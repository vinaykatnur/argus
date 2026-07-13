package com.argus.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateMonitorRequest(
        @NotBlank
        @Size(max = 2048)
        String url,

        @Size(max = 120)
        String displayName,

        @NotNull
        @Positive
        Integer intervalSeconds,

        @NotNull
        @Positive
        Integer failureThreshold,

        Boolean emailDowntimeNotificationsEnabled,

        Boolean emailRecoveryNotificationsEnabled,

        NotificationPreferencesRequest notificationPreferences
) {
}
