package com.argus.incidentintelligence.mapper;

import com.argus.incidentintelligence.dto.NarrativeSnapshotDto;
import com.argus.incidentintelligence.entity.IncidentNarrativeSnapshot;
import org.springframework.stereotype.Component;

@Component
public class IncidentNarrativeSnapshotMapper {

    public NarrativeSnapshotDto toDto(IncidentNarrativeSnapshot snapshot) {
        return new NarrativeSnapshotDto(
                snapshot.getId(),
                snapshot.getOwner().getId(),
                snapshot.getIncident().getId(),
                snapshot.getAnalysisSnapshot().getId(),
                snapshot.getVersion(),
                snapshot.getExecutiveSummary(),
                snapshot.getExplanation(),
                snapshot.getMarkdownPostMortem(),
                snapshot.getProviderName(),
                snapshot.getModelName(),
                snapshot.getStatus(),
                snapshot.getFailureReason(),
                snapshot.getGenerationDurationMillis(),
                snapshot.getTokenUsage(),
                snapshot.getGeneratedAt()
        );
    }
}
