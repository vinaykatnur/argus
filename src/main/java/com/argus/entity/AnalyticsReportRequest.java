package com.argus.entity;

import com.argus.enums.AnalyticsReportStatus;
import com.argus.enums.IncidentDateRange;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
        name = "analytics_report_requests",
        indexes = {
                @Index(name = "idx_analytics_reports_owner", columnList = "user_id"),
                @Index(name = "idx_analytics_reports_requested_at", columnList = "requested_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnalyticsReportRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IncidentDateRange dateRange;

    private Instant customStartDate;

    private Instant customEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnalyticsReportStatus status = AnalyticsReportStatus.PENDING;

    private String warningMessage;

    @Lob
    @Column(name = "result_json")
    private String resultJson;

    @Column(nullable = false, updatable = false)
    private Instant requestedAt;

    private Instant completedAt;

    public AnalyticsReportRequest(User owner, IncidentDateRange dateRange, Instant customStartDate, Instant customEndDate) {
        this.owner = owner;
        this.dateRange = dateRange;
        this.customStartDate = customStartDate;
        this.customEndDate = customEndDate;
    }

    @PrePersist
    @SuppressWarnings("unused")
    void onCreate() {
        requestedAt = Instant.now();
    }

    @PreUpdate
    @SuppressWarnings("unused")
    void onUpdate() {
        completedAt = status == AnalyticsReportStatus.READY || status == AnalyticsReportStatus.FAILED ? Instant.now() : completedAt;
    }
}
