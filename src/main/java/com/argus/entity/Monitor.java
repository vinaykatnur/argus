package com.argus.entity;

import com.argus.enums.MonitorStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Formula;

@Getter
@Setter
@Entity
@Table(
        name = "monitors",
        indexes = {
                @Index(name = "idx_monitors_owner", columnList = "user_id"),
                @Index(name = "idx_monitors_due", columnList = "active, check_in_progress, next_check_at"),
                @Index(name = "idx_monitors_status", columnList = "status")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 2048)
    private String url;

    @Column(length = 120)
    private String displayName;

    @Column(nullable = false)
    private int intervalSeconds;

    @Column(nullable = false)
    private int failureThreshold;

    @Column(nullable = false)
    private boolean emailDowntimeNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean emailRecoveryNotificationsEnabled = true;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MonitorStatus status = MonitorStatus.HEALTHY;

    @Column(nullable = false)
    private int consecutiveFailureCount;

    private Instant lastCheckedAt;

    private Instant lastSuccessfulCheckAt;

    private Long lastResponseTimeMillis;

    private Instant nextCheckAt;

    @Column(nullable = false)
    private boolean checkInProgress;

    @Column(nullable = false)
    private boolean pinned;

    private Integer pinnedPosition;

    @Formula("(select count(i.id) from incidents i where i.monitor_id = id)")
    private long incidentCount;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Monitor(User owner, String url, String displayName, int intervalSeconds, int failureThreshold) {
        this.owner = owner;
        this.url = url;
        this.displayName = displayName;
        this.intervalSeconds = intervalSeconds;
        this.failureThreshold = failureThreshold;
        this.nextCheckAt = Instant.now();
    }

    public boolean isPaused() {
        return status == MonitorStatus.PAUSED;
    }

    public boolean isEligibleForMonitoring() {
        return active && !isPaused();
    }

    public void scheduleNextCheck(Instant checkedAt) {
        nextCheckAt = checkedAt.plusSeconds(intervalSeconds);
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (nextCheckAt == null) {
            nextCheckAt = now;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
