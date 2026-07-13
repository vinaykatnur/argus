package com.argus.repository;

import com.argus.entity.MonitorHistory;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface MonitorHistoryRepository extends JpaRepository<MonitorHistory, Long> {

    List<MonitorHistory> findByMonitor_IdAndCheckedAtBetweenOrderByCheckedAtAsc(Long monitorId, Instant start, Instant end);

    List<MonitorHistory> findByCheckedAtBetweenOrderByCheckedAtAsc(Instant start, Instant end);

    @Transactional
    @Modifying
    @Query("delete from MonitorHistory h where h.checkedAt < :cutoff")
    void deleteByCheckedAtBefore(@Param("cutoff") Instant cutoff);

    @Transactional
    @Modifying
    void deleteByMonitor(com.argus.entity.Monitor monitor);
}
