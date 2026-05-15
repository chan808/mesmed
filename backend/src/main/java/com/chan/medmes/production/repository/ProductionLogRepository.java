package com.chan.medmes.production.repository;

import com.chan.medmes.production.entity.ProductionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductionLogRepository extends JpaRepository<ProductionLog, Long> {
    List<ProductionLog> findByLotId(Long lotId);

    // 주어진 시점 이후에 시작된 모든 생산 로그를 조회합니다.
    @Query("SELECT p FROM ProductionLog p WHERE p.startedAt >= :start")
    List<ProductionLog> findTodayLogs(LocalDateTime start);

    // 주어진 시점 이후 발생한 총 생산 수량을 합산하여 반환합니다.
    @Query("SELECT COALESCE(SUM(p.producedQty), 0) FROM ProductionLog p WHERE p.startedAt >= :start")
    int sumTodayProducedQty(LocalDateTime start);

    // 주어진 일자 이후의 일별 총 생산량을 집계합니다. (Native Query 사용)
    @Query(value = "SELECT CAST(started_at AS DATE) AS date, COALESCE(SUM(produced_qty), 0) AS qty " +
                   "FROM production_log WHERE started_at >= :start " +
                   "GROUP BY CAST(started_at AS DATE) ORDER BY CAST(started_at AS DATE)", nativeQuery = true)
    List<Object[]> findDailyProductionSince(LocalDateTime start);
}