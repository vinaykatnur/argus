package com.argus.incidentintelligence.entity;

import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import com.argus.entity.User;
import com.argus.incidentintelligence.enums.ConfidenceLevel;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "incident_analysis_snapshots",
        indexes = {
                @Index(name = "idx_analysis_snapshot_incident", columnList = "incident_id, version"),
                @Index(name = "idx_analysis_snapshot_owner", columnList = "owner_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_analysis_snapshot_incident_version", columnNames = {"incident_id", "version"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentAnalysisSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false, updatable = false)
    private Incident incident;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "monitor_id", nullable = false, updatable = false)
    private Monitor monitor;

    @Column(nullable = false, updatable = false)
    private int version;

    @Column(nullable = false, length = 40, updatable = false)
    private String engineVersion;

    @Column(nullable = false, length = 1000, updatable = false)
    private String incidentSummary;

    @Column(nullable = false, length = 1000, updatable = false)
    private String probablePattern;

    @Column(nullable = false, updatable = false)
    private int confidenceScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, updatable = false)
    private ConfidenceLevel confidenceLevel;

    @Column(nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String evidenceJson;

    @Column(nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String similarityJson;

    @Column(nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String confidenceJson;

    @Column(nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String recommendationsJson;

    @Column(nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String timelineJson;

    @Column(nullable = false, updatable = false)
    private Instant generatedAt;

    public IncidentAnalysisSnapshot(
            User owner,
            Incident incident,
            Monitor monitor,
            int version,
            String engineVersion,
            String incidentSummary,
            String probablePattern,
            int confidenceScore,
            ConfidenceLevel confidenceLevel,
            String evidenceJson,
            String similarityJson,
            String confidenceJson,
            String recommendationsJson,
            String timelineJson
    ) {
        this.owner = owner;
        this.incident = incident;
        this.monitor = monitor;
        this.version = version;
        this.engineVersion = engineVersion;
        this.incidentSummary = incidentSummary;
        this.probablePattern = probablePattern;
        this.confidenceScore = confidenceScore;
        this.confidenceLevel = confidenceLevel;
        this.evidenceJson = evidenceJson;
        this.similarityJson = similarityJson;
        this.confidenceJson = confidenceJson;
        this.recommendationsJson = recommendationsJson;
        this.timelineJson = timelineJson;
    }

    @PrePersist
    void onCreate() {
        generatedAt = Instant.now();
    }
}
