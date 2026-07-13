package com.argus.repository;

import com.argus.entity.AnalyticsSummary;
import com.argus.entity.Monitor;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface AnalyticsSummaryRepository extends JpaRepository<AnalyticsSummary, Long> {

    Optional<AnalyticsSummary> findByMonitorAndDate(Monitor monitor, LocalDate date);

    List<AnalyticsSummary> findByMonitor_IdAndDateBetweenOrderByDateAsc(Long monitorId, LocalDate startDate, LocalDate endDate);

    @Query("select a from AnalyticsSummary a where a.monitor.owner.id = :ownerId and a.date between :startDate and :endDate order by a.date asc")
    List<AnalyticsSummary> findByOwnerIdAndDateBetweenOrderByDateAsc(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("select case when sum(a.responseCount) = 0 then null else sum(a.sumResponseTimeMillis) * 1.0 / sum(a.responseCount) end from AnalyticsSummary a where a.monitor.owner.id = :ownerId and a.date between :startDate and :endDate")
    Double findAverageResponseTimeMillisByOwnerIdAndDateRange(
            @Param("ownerId") Long ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("select a from AnalyticsSummary a where a.monitor.owner.id = :ownerId and a.date = :date")
    List<AnalyticsSummary> findByOwnerIdAndDate(@Param("ownerId") Long ownerId, @Param("date") LocalDate date, Pageable pageable);

    @Query("select coalesce(sum(a.totalChecks), 0) from AnalyticsSummary a where a.monitor.owner.id = :ownerId and a.date between :startDate and :endDate")
    long sumTotalChecksByOwnerAndDateRange(@Param("ownerId") Long ownerId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Transactional
    @Modifying
    @Query("delete from AnalyticsSummary a where a.date < :cutoff")
    void deleteByDateBefore(@Param("cutoff") LocalDate cutoff);
}
