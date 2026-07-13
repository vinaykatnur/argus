package com.argus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "analytics_summaries",
        indexes = {
                @Index(name = "idx_analytics_summaries_monitor_date", columnList = "monitor_id, summary_date"),
                @Index(name = "idx_analytics_summaries_date", columnList = "summary_date")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalyticsSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Column(name = "summary_date", nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private long totalChecks;

    @Column(nullable = false)
    private long successfulChecks;

    @Column(nullable = false)
    private long responseCount;

    @Column(nullable = false)
    private long sumResponseTimeMillis;

    @Column(nullable = false)
    private double availabilityPercent;

    @Column(nullable = false)
    private long averageResponseTimeMillis;

    @Column(nullable = false)
    private long incidentCount;

    @Column(nullable = false)
    private long totalDowntimeMillis;

    @Column(nullable = false)
    private long averageIncidentDurationMillis;

    @Column(nullable = false)
    private long mtbfMillis;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public AnalyticsSummary(Monitor monitor, LocalDate date) {
        this.monitor = monitor;
        this.date = date;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
