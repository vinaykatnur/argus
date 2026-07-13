package com.argus.incidentintelligence.mapper;

import com.argus.incidentintelligence.dto.ConfidenceResultDto;
import com.argus.incidentintelligence.dto.EvidenceItemDto;
import com.argus.incidentintelligence.dto.IncidentAnalysisSnapshotDto;
import com.argus.incidentintelligence.dto.RecommendationDto;
import com.argus.incidentintelligence.dto.SimilarityResultDto;
import com.argus.incidentintelligence.dto.TimelineEventDto;
import com.argus.incidentintelligence.entity.IncidentAnalysisSnapshot;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class IncidentAnalysisSnapshotMapper {

    private static final TypeReference<List<EvidenceItemDto>> EVIDENCE_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<SimilarityResultDto> SIMILARITY_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<ConfidenceResultDto> CONFIDENCE_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<RecommendationDto>> RECOMMENDATIONS_TYPE = new TypeReference<>() {
    };
    private static final TypeReference<List<TimelineEventDto>> TIMELINE_TYPE = new TypeReference<>() {
    };

    private final IncidentIntelligenceJsonMapper jsonMapper;

    public IncidentAnalysisSnapshotMapper(IncidentIntelligenceJsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public IncidentAnalysisSnapshotDto toDto(IncidentAnalysisSnapshot snapshot) {
        return new IncidentAnalysisSnapshotDto(
                snapshot.getId(),
                snapshot.getOwner().getId(),
                snapshot.getIncident().getId(),
                snapshot.getMonitor().getId(),
                snapshot.getVersion(),
                snapshot.getEngineVersion(),
                snapshot.getIncidentSummary(),
                snapshot.getProbablePattern(),
                jsonMapper.read(snapshot.getEvidenceJson(), EVIDENCE_TYPE),
                jsonMapper.read(snapshot.getSimilarityJson(), SIMILARITY_TYPE),
                jsonMapper.read(snapshot.getConfidenceJson(), CONFIDENCE_TYPE),
                jsonMapper.read(snapshot.getRecommendationsJson(), RECOMMENDATIONS_TYPE),
                jsonMapper.read(snapshot.getTimelineJson(), TIMELINE_TYPE),
                snapshot.getGeneratedAt()
        );
    }
}
