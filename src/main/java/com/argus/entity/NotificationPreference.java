package com.argus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "notification_preferences",
        indexes = {
                @Index(name = "idx_notification_preferences_monitor", columnList = "monitor_id", unique = true)
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false, unique = true)
    private Monitor monitor;

    @Column(nullable = false)
    private boolean emailEnabled = true;

    @Column(nullable = false)
    private boolean downAlertsEnabled = true;

    @Column(nullable = false)
    private boolean recoveryAlertsEnabled = true;

    @Column(nullable = false)
    private boolean slowAlertsEnabled;

    @Column(nullable = false)
    private boolean reminderNotificationsEnabled;

    private Integer reminderFrequencyMinutes;

    @Column(nullable = false)
    private boolean quietHoursEnabled;

    private LocalTime quietHoursStart;

    private LocalTime quietHoursEnd;

    @Column(nullable = false)
    private boolean criticalMonitor;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public NotificationPreference(Monitor monitor) {
        this.monitor = monitor;
    }

    public boolean hasUsableReminderFrequency() {
        return reminderFrequencyMinutes != null && reminderFrequencyMinutes > 0;
    }

    public boolean hasCompleteQuietHours() {
        return quietHoursStart != null && quietHoursEnd != null;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
