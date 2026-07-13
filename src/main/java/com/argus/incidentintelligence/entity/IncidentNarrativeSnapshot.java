package com.argus.incidentintelligence.entity;

import com.argus.entity.Incident;
import com.argus.entity.User;
import com.argus.incidentintelligence.enums.AiProviderName;
import com.argus.incidentintelligence.enums.NarrativeGenerationStatus;
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
        name = "incident_narrative_snapshots",
        indexes = {
                @Index(name = "idx_narrative_snapshot_incident", columnList = "incident_id, version"),
                @Index(name = "idx_narrative_snapshot_owner", columnList = "owner_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_narrative_snapshot_incident_version", columnNames = {"incident_id", "version"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IncidentNarrativeSnapshot {

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
    @JoinColumn(name = "analysis_snapshot_id", nullable = false, updatable = false)
    private IncidentAnalysisSnapshot analysisSnapshot;

    @Column(nullable = false, updatable = false)
    private int version;

    @Column(length = 2000, updatable = false)
    private String executiveSummary;

    @Column(columnDefinition = "LONGTEXT", updatable = false)
    private String explanation;

    @Column(columnDefinition = "LONGTEXT", updatable = false)
    private String markdownPostMortem;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, updatable = false)
    private AiProviderName providerName;

    @Column(length = 120, updatable = false)
    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40, updatable = false)
    private NarrativeGenerationStatus status;

    @Column(length = 1000, updatable = false)
    private String failureReason;

    @Column(updatable = false)
    private Long generationDurationMillis;

    @Column(updatable = false)
    private Integer tokenUsage;

    @Column(nullable = false, updatable = false)
    private Instant generatedAt;

    public IncidentNarrativeSnapshot(
            User owner,
            Incident incident,
            IncidentAnalysisSnapshot analysisSnapshot,
            int version,
            String executiveSummary,
            String explanation,
            String markdownPostMortem,
            AiProviderName providerName,
            String modelName,
            NarrativeGenerationStatus status,
            String failureReason,
            Long generationDurationMillis,
            Integer tokenUsage
    ) {
        this.owner = owner;
        this.incident = incident;
        this.analysisSnapshot = analysisSnapshot;
        this.version = version;
        this.executiveSummary = executiveSummary;
        this.explanation = explanation;
        this.markdownPostMortem = markdownPostMortem;
        this.providerName = providerName;
        this.modelName = modelName;
        this.status = status;
        this.failureReason = failureReason;
        this.generationDurationMillis = generationDurationMillis;
        this.tokenUsage = tokenUsage;
    }

    @PrePersist
    void onCreate() {
        generatedAt = Instant.now();
    }
}
