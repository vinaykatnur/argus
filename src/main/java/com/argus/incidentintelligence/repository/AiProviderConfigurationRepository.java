package com.argus.incidentintelligence.repository;

import com.argus.incidentintelligence.entity.AiProviderConfiguration;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiProviderConfigurationRepository extends JpaRepository<AiProviderConfiguration, Long> {

    Optional<AiProviderConfiguration> findByOwner_Id(Long ownerId);

    void deleteByOwner_Id(Long ownerId);
}
