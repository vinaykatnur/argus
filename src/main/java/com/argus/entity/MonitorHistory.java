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
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
        name = "monitor_history",
        indexes = {
                @Index(name = "idx_monitor_history_monitor_checked", columnList = "monitor_id, checked_at"),
                @Index(name = "idx_monitor_history_checked_at", columnList = "checked_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MonitorHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false)
    private Monitor monitor;

    @Column(nullable = false)
    private Instant checkedAt;

    @Column(nullable = false)
    private boolean successful;

    private Integer httpStatusCode;

    private Long responseTimeMillis;

    @Column(length = 1000)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public MonitorHistory(Monitor monitor, Instant checkedAt, boolean successful, Integer httpStatusCode,
                          Long responseTimeMillis, String failureReason) {
        this.monitor = monitor;
        this.checkedAt = checkedAt;
        this.successful = successful;
        this.httpStatusCode = httpStatusCode;
        this.responseTimeMillis = responseTimeMillis;
        this.failureReason = failureReason;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        createdAt = Instant.now();
    }
}
