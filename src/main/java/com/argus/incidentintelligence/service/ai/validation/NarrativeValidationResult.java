package com.argus.incidentintelligence.service.ai.validation;

import java.util.List;

public record NarrativeValidationResult(
        boolean valid,
        List<String> reasons
) {
    public NarrativeValidationResult {
        reasons = List.copyOf(reasons);
    }

    public static NarrativeValidationResult validResult() {
        return new NarrativeValidationResult(true, List.of());
    }

    public static NarrativeValidationResult invalid(List<String> reasons) {
        return new NarrativeValidationResult(false, reasons);
    }
}
