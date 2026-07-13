package com.argus.repository;

import com.argus.entity.Monitor;
import com.argus.enums.MonitorStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MonitorRepository extends JpaRepository<Monitor, Long>, JpaSpecificationExecutor<Monitor> {

    List<Monitor> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Monitor> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerId(Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, MonitorStatus status);

    List<Monitor> findTop5ByOwnerIdAndPinnedTrueOrderByPinnedPositionAscIdAsc(Long ownerId);

    List<Monitor> findByActiveTrueAndCheckInProgressFalseAndNextCheckAtLessThanEqual(
            Instant nextCheckAt,
            Pageable pageable
    );

    @Query("select avg(m.lastResponseTimeMillis) from Monitor m where m.owner.id = :ownerId "
            + "and m.lastResponseTimeMillis is not null")
    Double findAverageResponseTimeMillisByOwnerId(@Param("ownerId") Long ownerId);

    @Query("select coalesce(max(m.pinnedPosition), 0) from Monitor m where m.owner.id = :ownerId and m.pinned = true")
    int findMaxPinnedPositionByOwnerId(@Param("ownerId") Long ownerId);

    @Query("select m from Monitor m where m.owner.id = :ownerId and "
            + "(m.status in :statuses or exists "
            + "(select i.id from Incident i where i.monitor = m and i.status = com.argus.enums.IncidentStatus.ACTIVE))")
    List<Monitor> findNeedsAttentionCandidates(
            @Param("ownerId") Long ownerId,
            @Param("statuses") List<MonitorStatus> statuses,
            Pageable pageable
    );
}
