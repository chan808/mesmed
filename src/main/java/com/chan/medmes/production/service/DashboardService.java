package com.chan.medmes.production.service;

import com.chan.medmes.material.LotStatus;
import com.chan.medmes.material.repository.LotRepository;
import com.chan.medmes.production.dto.DashboardResponse;
import com.chan.medmes.production.enums.EquipmentStatus;
import com.chan.medmes.production.repository.AlarmLogRepository;
import com.chan.medmes.production.repository.EquipmentRepository;
import com.chan.medmes.production.repository.ProductionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductionLogRepository productionLogRepository;
    private final AlarmLogRepository alarmLogRepository;
    private final EquipmentRepository equipmentRepository;
    private final LotRepository lotRepository;

    public DashboardResponse getDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        int todayProducedQty = productionLogRepository.sumTodayProducedQty(startOfDay);
        long activeAlarmCount = alarmLogRepository.countByResolvedAtIsNull();
        double equipmentRunningRate = calcRunningRate();
        long pendingLotCount = lotRepository.countByStatusAndDeletedAtIsNull(LotStatus.PENDING);

        return new DashboardResponse(todayProducedQty, activeAlarmCount, equipmentRunningRate, pendingLotCount);
    }

    // 전체 설비 중 RUNNING 비율 (설비가 없으면 0.0)
    private double calcRunningRate() {
        long total = equipmentRepository.count();
        if (total == 0) return 0.0;
        long running = equipmentRepository.countByStatus(EquipmentStatus.RUNNING);
        return (double) running / total;
    }
}