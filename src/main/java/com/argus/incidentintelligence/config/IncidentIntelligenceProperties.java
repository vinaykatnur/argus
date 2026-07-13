package com.argus.incidentintelligence.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "argus.incident-intelligence")
public record IncidentIntelligenceProperties(
        Ai ai
) {
    public IncidentIntelligenceProperties {
        if (ai == null) {
            ai = new Ai("", 20);
        }
    }

    public record Ai(
            String encryptionSecret,
            int timeoutSeconds
    ) {
        public Ai {
            if (timeoutSeconds <= 0) {
                timeoutSeconds = 20;
            }
        }
    }
}
