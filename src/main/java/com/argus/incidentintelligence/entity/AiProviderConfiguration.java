package com.argus.incidentintelligence.entity;

import com.argus.entity.User;
import com.argus.incidentintelligence.enums.AiProviderName;
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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "ai_provider_configurations",
        indexes = {
                @Index(name = "idx_ai_provider_config_owner", columnList = "owner_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ai_provider_config_owner", columnNames = "owner_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiProviderConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private AiProviderName providerName;

    @Column(nullable = false, length = 120)
    private String modelName;

    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String encryptedApiKey;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    public AiProviderConfiguration(
            User owner,
            AiProviderName providerName,
            String modelName,
            String encryptedApiKey
    ) {
        this.owner = owner;
        update(providerName, modelName, encryptedApiKey);
    }

    public void update(AiProviderName providerName, String modelName, String encryptedApiKey) {
        this.providerName = providerName;
        this.modelName = modelName;
        this.encryptedApiKey = encryptedApiKey;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
