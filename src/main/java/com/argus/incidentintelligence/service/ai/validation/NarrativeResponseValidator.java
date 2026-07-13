package com.argus.incidentintelligence.service.ai.validation;

import com.argus.incidentintelligence.dto.IncidentAnalysisSnapshotDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class NarrativeResponseValidator {

    private static final List<String> GUARDED_TERMS = List.of(
            "cloudflare",
            "database",
            "mysql",
            "postgres",
            "redis",
            "kubernetes",
            "load balancer",
            "aws",
            "azure",
            "gcp",
            "nginx",
            "apache",
            "cdn"
    );

    public NarrativeValidationResult validate(IncidentAnalysisSnapshotDto snapshot, String responseText) {
        List<String> failures = new ArrayList<>();
        if (responseText == null || responseText.isBlank()) {
            failures.add("Provider returned an empty narrative.");
            return NarrativeValidationResult.invalid(failures);
        }

        String response = responseText.toLowerCase(Locale.ROOT);
        String deterministic = deterministicText(snapshot).toLowerCase(Locale.ROOT);

        for (String guardedTerm : GUARDED_TERMS) {
            if (response.contains(guardedTerm) && !deterministic.contains(guardedTerm)) {
                failures.add("Narrative introduced unsupported infrastructure or service: " + guardedTerm + ".");
            }
        }

        if (response.contains("100%") || response.contains("certainly") || response.contains("guaranteed")) {
            failures.add("Narrative claimed certainty beyond deterministic confidence.");
        }

        if (response.contains("restart ")) {
            failures.add("Narrative prescribed an operational action instead of investigation.");
        }

        if (!response.contains(snapshot.confidence().level().name().toLowerCase(Locale.ROOT).replace('_', ' '))
                && !response.contains(snapshot.confidence().level().name().toLowerCase(Locale.ROOT))) {
            failures.add("Narrative did not preserve deterministic confidence level.");
        }

        return failures.isEmpty() ? NarrativeValidationResult.validResult() : NarrativeValidationResult.invalid(failures);
    }

    private String deterministicText(IncidentAnalysisSnapshotDto snapshot) {
        StringBuilder builder = new StringBuilder(snapshot.incidentSummary())
                .append(' ')
                .append(snapshot.probablePattern());
        snapshot.evidence().forEach(item -> builder.append(' ').append(item.observation()));
        snapshot.recommendations().forEach(item -> builder.append(' ')
                .append(item.recommendation()).append(' ').append(item.reason()));
        snapshot.timeline().forEach(item -> builder.append(' ').append(item.description()));
        return builder.toString();
    }
}
