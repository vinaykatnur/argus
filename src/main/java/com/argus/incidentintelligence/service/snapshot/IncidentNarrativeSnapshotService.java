package com.argus.incidentintelligence.service.snapshot;

import com.argus.entity.Incident;
import com.argus.exception.ResourceNotFoundException;
import com.argus.incidentintelligence.dto.NarrativeSnapshotDto;
import com.argus.incidentintelligence.entity.IncidentAnalysisSnapshot;
import com.argus.incidentintelligence.entity.IncidentNarrativeSnapshot;
import com.argus.incidentintelligence.enums.AiProviderName;
import com.argus.incidentintelligence.enums.NarrativeGenerationStatus;
import com.argus.incidentintelligence.mapper.IncidentNarrativeSnapshotMapper;
import com.argus.incidentintelligence.repository.IncidentAnalysisSnapshotRepository;
import com.argus.incidentintelligence.repository.IncidentNarrativeSnapshotRepository;
import com.argus.repository.IncidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IncidentNarrativeSnapshotService {

    private final IncidentRepository incidentRepository;
    private final IncidentAnalysisSnapshotRepository analysisSnapshotRepository;
    private final IncidentNarrativeSnapshotRepository narrativeSnapshotRepository;
    private final IncidentNarrativeSnapshotMapper mapper;

    public IncidentNarrativeSnapshotService(
            IncidentRepository incidentRepository,
            IncidentAnalysisSnapshotRepository analysisSnapshotRepository,
            IncidentNarrativeSnapshotRepository narrativeSnapshotRepository,
            IncidentNarrativeSnapshotMapper mapper
    ) {
        this.incidentRepository = incidentRepository;
        this.analysisSnapshotRepository = analysisSnapshotRepository;
        this.narrativeSnapshotRepository = narrativeSnapshotRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public NarrativeSnapshotDto latest(Long ownerId, Long incidentId) {
        return narrativeSnapshotRepository.findTopByIncident_IdAndOwner_IdOrderByVersionDesc(incidentId, ownerId)
                .map(mapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Incident narrative snapshot not found"));
    }

    @Transactional
    public NarrativeSnapshotDto persist(
            Long ownerId,
            Long incidentId,
            Long analysisSnapshotId,
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
        Incident incident = incidentRepository.findByIdAndMonitor_Owner_Id(incidentId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found"));
        IncidentAnalysisSnapshot analysisSnapshot = analysisSnapshotRepository.findById(analysisSnapshotId)
                .filter(snapshot -> snapshot.getOwner().getId().equals(ownerId))
                .filter(snapshot -> snapshot.getIncident().getId().equals(incidentId))
                .orElseThrow(() -> new ResourceNotFoundException("Incident analysis snapshot not found"));
        int version = narrativeSnapshotRepository.findMaxVersionByIncidentId(incidentId) + 1;
        IncidentNarrativeSnapshot snapshot = new IncidentNarrativeSnapshot(
                incident.getMonitor().getOwner(),
                incident,
                analysisSnapshot,
                version,
                executiveSummary,
                explanation,
                markdownPostMortem,
                providerName,
                modelName,
                status,
                failureReason,
                generationDurationMillis,
                tokenUsage
        );
        return mapper.toDto(narrativeSnapshotRepository.save(snapshot));
    }
}
