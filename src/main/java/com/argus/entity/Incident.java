package com.argus.entity;

import com.argus.enums.IncidentStatus;
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
import java.time.Duration;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "incidents",
        indexes = {
                @Index(name = "idx_incidents_monitor_status", columnList = "monitor_id, status"),
                @Index(name = "idx_incidents_started_at", columnList = "started_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Column(nullable = false)
    private Instant startedAt;

    private Instant resolvedAt;

    private Long downtimeMillis;

    @Column(length = 1000)
    private String failureReason;

    private Integer consecutiveFailedChecks;

    private Instant lastSuccessfulCheckAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentStatus status = IncidentStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public Incident(Monitor monitor, Instant startedAt) {
        this.monitor = monitor;
        this.startedAt = startedAt;
    }

    public Incident(
            Monitor monitor,
            Instant startedAt,
            String failureReason,
            Integer consecutiveFailedChecks,
            Instant lastSuccessfulCheckAt
    ) {
        this.monitor = monitor;
        this.startedAt = startedAt;
        this.failureReason = failureReason;
        this.consecutiveFailedChecks = consecutiveFailedChecks;
        this.lastSuccessfulCheckAt = lastSuccessfulCheckAt;
    }

    public void resolve(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
        this.downtimeMillis = Duration.between(startedAt, resolvedAt).toMillis();
        this.status = IncidentStatus.RESOLVED;
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
