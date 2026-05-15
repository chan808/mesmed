package com.chan.medmes.production.service;

import com.chan.medmes.material.enums.LotStatus;
import com.chan.medmes.material.repository.LotRepository;
import com.chan.medmes.production.dto.DailyProductionDto;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ProductionLogRepository productionLogRepository;
    private final AlarmLogRepository alarmLogRepository;
    private final EquipmentRepository equipmentRepository;
    private final LotRepository lotRepository;

    // 금일 총 생산량, 활성 알람 수, 설비 가동 상태 및 Lot 현황 등을 집계하여 대시보드 데이터를 생성합니다.
    public DashboardResponse getDashboard() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();

        int todayProducedQty = productionLogRepository.sumTodayProducedQty(startOfDay);
        long activeAlarmCount = alarmLogRepository.countByResolvedAtIsNull();
        long totalEquipment = equipmentRepository.count();
        long runningEquipment = equipmentRepository.countByStatus(EquipmentStatus.RUNNING);
        double equipmentRunningRate = totalEquipment == 0 ? 0.0 : (double) runningEquipment / totalEquipment;
        long pendingLotCount = lotRepository.countByStatusAndDeletedAtIsNull(LotStatus.PENDING);
        long passLotCount = lotRepository.countByStatusAndDeletedAtIsNull(LotStatus.PASS);
        long failLotCount = lotRepository.countByStatusAndDeletedAtIsNull(LotStatus.FAIL);
        long holdLotCount = lotRepository.countByStatusAndDeletedAtIsNull(LotStatus.HOLD);

        return new DashboardResponse(
                todayProducedQty, activeAlarmCount, equipmentRunningRate,
                pendingLotCount, runningEquipment, totalEquipment,
                passLotCount, failLotCount, holdLotCount
        );
    }

    // 최근 7일간의 일자별 총 생산량 추이 데이터를 조회합니다. 빈 날짜는 0으로 채워 반환합니다.
    public List<DailyProductionDto> getDailyProduction() {
        LocalDateTime start = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> rows = productionLogRepository.findDailyProductionSince(start);

        Map<String, Integer> dataMap = rows.stream().collect(Collectors.toMap(
                row -> row[0].toString(),
                row -> ((Number) row[1]).intValue()
        ));

        // 최근 7일치 — DB에 데이터 없는 날은 0으로 채움
        return IntStream.rangeClosed(0, 6)
                .mapToObj(i -> {
                    String date = LocalDate.now().minusDays(6 - i).toString();
                    return new DailyProductionDto(date, dataMap.getOrDefault(date, 0));
                })
                .toList();
    }
}