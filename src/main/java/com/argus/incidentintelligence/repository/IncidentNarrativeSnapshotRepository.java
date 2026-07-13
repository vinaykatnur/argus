package com.argus.incidentintelligence.repository;

import com.argus.incidentintelligence.entity.IncidentNarrativeSnapshot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentNarrativeSnapshotRepository extends JpaRepository<IncidentNarrativeSnapshot, Long> {

    Optional<IncidentNarrativeSnapshot> findTopByIncident_IdAndOwner_IdOrderByVersionDesc(Long incidentId, Long ownerId);

    @Query("select coalesce(max(s.version), 0) from IncidentNarrativeSnapshot s where s.incident.id = :incidentId")
    int findMaxVersionByIncidentId(@Param("incidentId") Long incidentId);
}
