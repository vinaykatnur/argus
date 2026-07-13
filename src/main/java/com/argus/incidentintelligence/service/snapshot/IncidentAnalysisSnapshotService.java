package com.argus.incidentintelligence.service.snapshot;

import com.argus.entity.Incident;
import com.argus.exception.ResourceNotFoundException;
import com.argus.incidentintelligence.dto.ConfidenceResultDto;
import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.IncidentAnalysisSnapshotDto;
import com.argus.incidentintelligence.dto.IncidentContext;
import com.argus.incidentintelligence.dto.RecommendationDto;
import com.argus.incidentintelligence.dto.SimilarityResultDto;
import com.argus.incidentintelligence.dto.TimelineEventDto;
import com.argus.incidentintelligence.entity.IncidentAnalysisSnapshot;
import com.argus.incidentintelligence.mapper.IncidentAnalysisSnapshotMapper;
import com.argus.incidentintelligence.mapper.IncidentIntelligenceJsonMapper;
import com.argus.incidentintelligence.repository.IncidentAnalysisSnapshotRepository;
import com.argus.repository.IncidentRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentAnalysisSnapshotService {

    private static final String ENGINE_VERSION = "phase-6.0";

    private final IncidentRepository incidentRepository;
    private final IncidentAnalysisSnapshotRepository snapshotRepository;
    private final IncidentAnalysisSnapshotMapper snapshotMapper;
    private final IncidentIntelligenceJsonMapper jsonMapper;

    public IncidentAnalysisSnapshotService(
            IncidentRepository incidentRepository,
            IncidentAnalysisSnapshotRepository snapshotRepository,
            IncidentAnalysisSnapshotMapper snapshotMapper,
            IncidentIntelligenceJsonMapper jsonMapper
    ) {
        this.incidentRepository = incidentRepository;
        this.snapshotRepository = snapshotRepository;
        this.snapshotMapper = snapshotMapper;
        this.jsonMapper = jsonMapper;
    }

    @Transactional(readOnly = true)
    public IncidentAnalysisSnapshotDto latest(Long ownerId, Long incidentId) {
        return snapshotRepository.findTopByIncident_IdAndOwner_IdOrderByVersionDesc(incidentId, ownerId)
                .map(snapshotMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Incident analysis snapshot not found"));
    }

    @Transactional
    public IncidentAnalysisSnapshotDto persist(
            Long ownerId,
            IncidentContext context,
            String probablePattern,
            List<EvidenceItemDto> evidence,
            SimilarityResultDto similarity,
            ConfidenceResultDto confidence,
            List<RecommendationDto> recommendations,
            List<TimelineEventDto> timeline
    ) {
        Incident incident = incidentRepository.findByIdAndMonitor_Owner_Id(context.incident().incidentId(), ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        int version = snapshotRepository.findMaxVersionByIncidentId(incident.getId()) + 1;
        IncidentAnalysisSnapshot snapshot = new IncidentAnalysisSnapshot(
                incident.getMonitor().getOwner(),
                incident,
                incident.getMonitor(),
                version,
                ENGINE_VERSION,
                incidentSummary(context),
                probablePattern,
                confidence.score(),
                confidence.level(),
                jsonMapper.write(evidence),
                jsonMapper.write(similarity),
                jsonMapper.write(confidence),
                jsonMapper.write(recommendations),
                jsonMapper.write(timeline)
        );
        return snapshotMapper.toDto(snapshotRepository.save(snapshot));
    }

    private String incidentSummary(IncidentContext context) {
        String target = context.monitor().displayName() == null
                ? context.monitor().url()
                : context.monitor().displayName();
        if (context.incident().resolvedAt() == null) {
            return "Incident for " + target + " opened at " + context.incident().startedAt()
                    + " and is currently active.";
        }
        long millis = Duration.between(context.incident().startedAt(), context.incident().resolvedAt()).toMillis();
        return "Incident for " + target + " opened at " + context.incident().startedAt()
                + " and resolved at " + context.incident().resolvedAt()
                + " after " + millis + " ms.";
    }
}
