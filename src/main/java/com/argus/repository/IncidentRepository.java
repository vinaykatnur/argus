package com.argus.repository;

import com.argus.entity.Incident;
import com.argus.entity.Monitor;
import com.argus.enums.IncidentStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentRepository extends JpaRepository<Incident, Long>, JpaSpecificationExecutor<Incident> {

    Optional<Incident> findFirstByMonitorAndStatusOrderByStartedAtDesc(Monitor monitor, IncidentStatus status);

    Optional<Incident> findFirstByMonitor_IdAndMonitor_Owner_IdAndStatusOrderByStartedAtDesc(
            Long monitorId,
            Long ownerId,
            IncidentStatus status
    );

    Optional<Incident> findByIdAndMonitor_Owner_Id(Long incidentId, Long ownerId);

    List<Incident> findByStatus(IncidentStatus status);

    List<Incident> findTop5ByMonitor_Owner_IdOrderByStartedAtDesc(Long ownerId);

    List<Incident> findTop5ByMonitor_IdAndMonitor_Owner_IdOrderByStartedAtDesc(Long monitorId, Long ownerId);

    List<Incident> findTop20ByMonitor_Owner_IdAndIdNotOrderByStartedAtDesc(Long ownerId, Long incidentId);

    long countByMonitor_Owner_IdAndStatus(Long ownerId, IncidentStatus status);

    void deleteByMonitor(Monitor monitor);

    @Query("select i from Incident i where i.monitor.owner.id = :ownerId order by i.startedAt desc")
    List<Incident> findRecentByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select distinct i.monitor.id from Incident i where i.monitor.owner.id = :ownerId "
            + "and i.monitor.id in :monitorIds and i.status = com.argus.enums.IncidentStatus.ACTIVE")
    List<Long> findActiveMonitorIds(
            @Param("ownerId") Long ownerId,
            @Param("monitorIds") List<Long> monitorIds
    );

    @Query("select i from Incident i where i.monitor.owner.id = :ownerId and i.startedAt between :startDate and :endDate")
    List<Incident> findByOwnerIdAndStartedAtBetween(
            @Param("ownerId") Long ownerId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    @Query("select i from Incident i where i.monitor.id = :monitorId and i.startedAt between :startDate and :endDate")
    List<Incident> findByMonitorIdAndStartedAtBetween(
            @Param("monitorId") Long monitorId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
}
