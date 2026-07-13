package com.argus.repository;

import com.argus.entity.Alert;
import com.argus.entity.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlertRepository extends JpaRepository<Alert, Long> {

    void deleteByMonitor(Monitor monitor);

    @Query("select avg(a.responseTimeMillis) from Alert a where a.monitor.id = :monitorId "
            + "and a.monitor.owner.id = :ownerId and a.responseTimeMillis is not null")
    Double findAverageResponseTimeMillis(@Param("monitorId") Long monitorId, @Param("ownerId") Long ownerId);

    @Query("select min(a.responseTimeMillis) from Alert a where a.monitor.id = :monitorId "
            + "and a.monitor.owner.id = :ownerId and a.responseTimeMillis is not null")
    Long findFastestResponseTimeMillis(@Param("monitorId") Long monitorId, @Param("ownerId") Long ownerId);

    @Query("select max(a.responseTimeMillis) from Alert a where a.monitor.id = :monitorId "
            + "and a.monitor.owner.id = :ownerId and a.responseTimeMillis is not null")
    Long findSlowestResponseTimeMillis(@Param("monitorId") Long monitorId, @Param("ownerId") Long ownerId);
}
