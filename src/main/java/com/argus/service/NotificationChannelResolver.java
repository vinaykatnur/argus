package com.argus.service;

import com.argus.entity.NotificationPreference;
import com.argus.enums.NotificationChannel;
import com.argus.enums.NotificationType;
import java.util.EnumSet;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class NotificationChannelResolver {

    public Set<NotificationChannel> resolve(NotificationPreference preference, NotificationType type) {
        if (!preference.isEmailEnabled() || !typeEnabled(preference, type)) {
            return Set.of();
        }
        return EnumSet.of(NotificationChannel.EMAIL);
    }

    private boolean typeEnabled(NotificationPreference preference, NotificationType type) {
        return switch (type) {
            case WEBSITE_DOWN -> preference.isDownAlertsEnabled();
            case WEBSITE_DOWN_REMINDER -> preference.isDownAlertsEnabled()
                    && preference.isReminderNotificationsEnabled()
                    && preference.hasUsableReminderFrequency();
            case WEBSITE_RECOVERED -> preference.isRecoveryAlertsEnabled();
            case SLOW_RESPONSE -> preference.isSlowAlertsEnabled();
        };
    }
}
