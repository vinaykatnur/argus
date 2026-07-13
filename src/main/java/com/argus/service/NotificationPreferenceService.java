package com.argus.service;

import com.argus.entity.Monitor;
import com.argus.entity.NotificationPreference;
import com.argus.repository.NotificationPreferenceRepository;
import com.argus.request.CreateMonitorRequest;
import com.argus.request.NotificationPreferencesRequest;
import com.argus.request.UpdateMonitorRequest;
import java.time.LocalTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public NotificationPreferenceService(NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    @Transactional
    public NotificationPreference configure(Monitor monitor, CreateMonitorRequest request) {
        NotificationPreference preference = findOrCreate(monitor);
        apply(preference, request.notificationPreferences());
        applyLegacyFlags(preference, request.emailDowntimeNotificationsEnabled(), request.emailRecoveryNotificationsEnabled());
        mirrorLegacyMonitorFields(monitor, preference);
        return notificationPreferenceRepository.save(preference);
    }

    @Transactional
    public NotificationPreference configure(Monitor monitor, UpdateMonitorRequest request) {
        NotificationPreference preference = findOrCreate(monitor);
        apply(preference, request.notificationPreferences());
        applyLegacyFlags(preference, request.emailDowntimeNotificationsEnabled(), request.emailRecoveryNotificationsEnabled());
        mirrorLegacyMonitorFields(monitor, preference);
        return notificationPreferenceRepository.save(preference);
    }

    @Transactional
    public NotificationPreference configure(Monitor monitor, NotificationPreferencesRequest request) {
        NotificationPreference preference = findOrCreate(monitor);
        apply(preference, request);
        mirrorLegacyMonitorFields(monitor, preference);
        return notificationPreferenceRepository.save(preference);
    }

    @Transactional
    public NotificationPreference getOrCreate(Monitor monitor) {
        NotificationPreference preference = findOrCreate(monitor);
        mirrorLegacyMonitorFields(monitor, preference);
        return notificationPreferenceRepository.save(preference);
    }

    @Transactional(readOnly = true)
    public NotificationPreference findExistingOrDefault(Monitor monitor) {
        return notificationPreferenceRepository.findByMonitor(monitor)
                .orElseGet(() -> defaultPreference(monitor));
    }

    public boolean isQuietNow(NotificationPreference preference, LocalTime now) {
        if (!preference.isQuietHoursEnabled() || preference.isCriticalMonitor() || !preference.hasCompleteQuietHours()) {
            return false;
        }

        LocalTime start = preference.getQuietHoursStart();
        LocalTime end = preference.getQuietHoursEnd();
        if (start.equals(end)) {
            return true;
        }
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        return !now.isBefore(start) || now.isBefore(end);
    }

    private NotificationPreference findOrCreate(Monitor monitor) {
        return notificationPreferenceRepository.findByMonitor(monitor)
                .orElseGet(() -> defaultPreference(monitor));
    }

    private NotificationPreference defaultPreference(Monitor monitor) {
        NotificationPreference preference = new NotificationPreference(monitor);
        preference.setEmailEnabled(monitor.isEmailDowntimeNotificationsEnabled()
                || monitor.isEmailRecoveryNotificationsEnabled());
        preference.setDownAlertsEnabled(monitor.isEmailDowntimeNotificationsEnabled());
        preference.setRecoveryAlertsEnabled(monitor.isEmailRecoveryNotificationsEnabled());
        return preference;
    }

    private void apply(NotificationPreference preference, NotificationPreferencesRequest request) {
        if (request == null) {
            return;
        }

        preference.setEmailEnabled(resolve(request.emailEnabled(), preference.isEmailEnabled()));
        preference.setDownAlertsEnabled(resolve(request.downAlertsEnabled(), preference.isDownAlertsEnabled()));
        preference.setRecoveryAlertsEnabled(resolve(
                request.recoveryAlertsEnabled(),
                preference.isRecoveryAlertsEnabled()
        ));
        preference.setSlowAlertsEnabled(resolve(request.slowAlertsEnabled(), preference.isSlowAlertsEnabled()));
        preference.setReminderNotificationsEnabled(resolve(
                request.reminderNotificationsEnabled(),
                preference.isReminderNotificationsEnabled()
        ));
        preference.setReminderFrequencyMinutes(request.reminderFrequencyMinutes());
        preference.setQuietHoursEnabled(resolve(request.quietHoursEnabled(), preference.isQuietHoursEnabled()));
        preference.setQuietHoursStart(request.quietHoursStart());
        preference.setQuietHoursEnd(request.quietHoursEnd());
        preference.setCriticalMonitor(resolve(request.criticalMonitor(), preference.isCriticalMonitor()));
    }

    private void applyLegacyFlags(
            NotificationPreference preference,
            Boolean emailDowntimeNotificationsEnabled,
            Boolean emailRecoveryNotificationsEnabled
    ) {
        if (emailDowntimeNotificationsEnabled != null) {
            preference.setDownAlertsEnabled(emailDowntimeNotificationsEnabled);
        }
        if (emailRecoveryNotificationsEnabled != null) {
            preference.setRecoveryAlertsEnabled(emailRecoveryNotificationsEnabled);
        }
        if (emailDowntimeNotificationsEnabled != null || emailRecoveryNotificationsEnabled != null) {
            preference.setEmailEnabled(preference.isDownAlertsEnabled() || preference.isRecoveryAlertsEnabled());
        }
    }

    private void mirrorLegacyMonitorFields(Monitor monitor, NotificationPreference preference) {
        monitor.setEmailDowntimeNotificationsEnabled(preference.isEmailEnabled() && preference.isDownAlertsEnabled());
        monitor.setEmailRecoveryNotificationsEnabled(preference.isEmailEnabled() && preference.isRecoveryAlertsEnabled());
    }

    private boolean resolve(Boolean value, boolean fallback) {
        return value == null ? fallback : value;
    }
}
