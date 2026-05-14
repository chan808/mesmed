package com.chan.medmes.production.repository;

import com.chan.medmes.production.entity.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    List<ProductionLog> findByLotId(Long lotId);

    @Query("SELECT p FROM ProductionLog p WHERE p.startedAt >= :start")
    List<ProductionLog> findTodayLogs(LocalDateTime start);

    @Query("SELECT COALESCE(SUM(p.producedQty), 0) FROM ProductionLog p WHERE p.startedAt >= :start")
    int sumTodayProducedQty(LocalDateTime start);
}