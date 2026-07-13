package com.argus.incidentintelligence.controller;

import com.argus.incidentintelligence.dto.IncidentAnalysisSnapshotDto;
import com.argus.incidentintelligence.dto.NarrativeSnapshotDto;
import com.argus.incidentintelligence.dto.RecommendationDto;
import com.argus.incidentintelligence.dto.SimilarIncidentDto;
import com.argus.incidentintelligence.dto.TimelineEventDto;
import com.argus.incidentintelligence.service.ai.narrative.IncidentNarrativeGenerationService;
import com.argus.incidentintelligence.service.snapshot.IncidentAnalysisSnapshotService;
import com.argus.incidentintelligence.service.snapshot.IncidentNarrativeSnapshotService;
import com.argus.security.ArgusUserDetails;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/incidents/{incidentId}")
public class IncidentIntelligenceController {

    private final IncidentAnalysisSnapshotService analysisSnapshotService;
    private final IncidentNarrativeSnapshotService narrativeSnapshotService;
    private final IncidentNarrativeGenerationService narrativeGenerationService;

    public IncidentIntelligenceController(
            IncidentAnalysisSnapshotService analysisSnapshotService,
            IncidentNarrativeSnapshotService narrativeSnapshotService,
            IncidentNarrativeGenerationService narrativeGenerationService
    ) {
        this.analysisSnapshotService = analysisSnapshotService;
        this.narrativeSnapshotService = narrativeSnapshotService;
        this.narrativeGenerationService = narrativeGenerationService;
    }

    @GetMapping("/analysis")
    public ResponseEntity<IncidentAnalysisSnapshotDto> analysis(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(analysisSnapshotService.latest(userDetails.getUser().getId(), incidentId));
    }

    @GetMapping("/timeline")
    public ResponseEntity<List<TimelineEventDto>> timeline(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(analysisSnapshotService.latest(userDetails.getUser().getId(), incidentId).timeline());
    }

    @GetMapping("/recommendations")
    public ResponseEntity<List<RecommendationDto>> recommendations(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(analysisSnapshotService.latest(userDetails.getUser().getId(), incidentId).recommendations());
    }

    @GetMapping("/similar-incidents")
    public ResponseEntity<List<SimilarIncidentDto>> similarIncidents(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(analysisSnapshotService.latest(userDetails.getUser().getId(), incidentId).similarity().matches());
    }

    @GetMapping("/narrative")
    public ResponseEntity<NarrativeSnapshotDto> narrative(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(narrativeSnapshotService.latest(userDetails.getUser().getId(), incidentId));
    }

    @PostMapping("/narrative")
    public ResponseEntity<NarrativeSnapshotDto> generateNarrative(
            @AuthenticationPrincipal ArgusUserDetails userDetails,
            @PathVariable Long incidentId
    ) {
        return ResponseEntity.ok(narrativeGenerationService.generate(userDetails.getUser().getId(), incidentId));
    }
}
