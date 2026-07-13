package com.argus.incidentintelligence.repository;

import com.argus.incidentintelligence.entity.IncidentAnalysisSnapshot;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentAnalysisSnapshotRepository extends JpaRepository<IncidentAnalysisSnapshot, Long> {

    Optional<IncidentAnalysisSnapshot> findTopByIncident_IdAndOwner_IdOrderByVersionDesc(Long incidentId, Long ownerId);

    @Query("select coalesce(max(s.version), 0) from IncidentAnalysisSnapshot s where s.incident.id = :incidentId")
    int findMaxVersionByIncidentId(@Param("incidentId") Long incidentId);
}
