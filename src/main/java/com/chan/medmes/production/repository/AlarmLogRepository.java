package com.chan.medmes.production.repository;

import com.chan.medmes.production.entity.AlarmLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmLogRepository extends JpaRepository<AlarmLog, Long> {
    List<AlarmLog> findByEquipmentIdOrderByOccurredAtDesc(Long equipmentId);
    List<AlarmLog> findByResolvedAtIsNullOrderByOccurredAtDesc();
}