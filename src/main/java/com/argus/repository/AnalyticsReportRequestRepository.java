package com.argus.repository;

import com.argus.entity.AnalyticsReportRequest;
import com.argus.enums.AnalyticsReportStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AnalyticsReportRequestRepository extends JpaRepository<AnalyticsReportRequest, Long> {

    List<AnalyticsReportRequest> findByOwner_IdOrderByRequestedAtDesc(Long ownerId);

    @Query("select a from AnalyticsReportRequest a where a.status = :status")
    List<AnalyticsReportRequest> findByStatus(@Param("status") AnalyticsReportStatus status);

    @Transactional
    @Modifying
    @Query("delete from AnalyticsReportRequest a where a.requestedAt < :cutoff")
    void deleteByRequestedAtBefore(@Param("cutoff") Instant cutoff);

    Optional<AnalyticsReportRequest> findByIdAndOwner_Id(Long id, Long ownerId);
}
