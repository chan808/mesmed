package com.chan.medmes.production.service;

import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.material.enums.LotStatus;
import com.chan.medmes.material.entity.Lot;
import com.chan.medmes.material.service.MaterialService;
import com.chan.medmes.production.dto.*;
import com.chan.medmes.production.entity.AlarmLog;
import com.chan.medmes.production.entity.Equipment;
import com.chan.medmes.production.entity.ProductionLog;
import com.chan.medmes.production.error.ProductionErrorCode;
import com.chan.medmes.production.repository.AlarmLogRepository;
import com.chan.medmes.production.repository.EquipmentRepository;
import com.chan.medmes.production.repository.ProductionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductionService {

    private final ProductionLogRepository productionLogRepository;
    private final EquipmentRepository equipmentRepository;
    private final AlarmLogRepository alarmLogRepository;
    private final MaterialService materialService;

    // ── Equipment ─────────────────────────────────────────────────

    // 새로운 설비를 등록하며, 설비 코드의 중복을 검증합니다.
    @Transactional
    public EquipmentResponse createEquipment(EquipmentRequest request) {
        if (equipmentRepository.existsByEquipmentCode(request.equipmentCode())) {
            throw new BusinessException(ProductionErrorCode.EQUIPMENT_CODE_DUPLICATED);
        }
        Equipment equipment = Equipment.builder()
                .equipmentCode(request.equipmentCode())
                .name(request.name())
                .build();
        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    public List<EquipmentResponse> getAllEquipment() {
        return equipmentRepository.findAll().stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    // 설비의 현재 운영 상태(RUNNING, STOPPED, MAINTENANCE 등)를 변경합니다.
    @Transactional
    public EquipmentResponse updateEquipmentStatus(Long id, EquipmentStatusRequest request) {
        Equipment equipment = findEquipmentEntityById(id);
        equipment.updateStatus(request.status());
        return EquipmentResponse.from(equipment);
    }

    public Equipment findEquipmentEntityById(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductionErrorCode.EQUIPMENT_NOT_FOUND));
    }

    // ── ProductionLog ─────────────────────────────────────────────

    // 합격(PASS) 상태인 Lot에 대해 특정 설비에서의 생산 및 불량 내역(생산 로그)을 기록합니다.
    @Transactional
    public ProductionLogResponse createProductionLog(ProductionLogRequest request) {
        Lot lot = materialService.findLotEntityById(request.lotId());

        if (lot.getStatus() != LotStatus.PASS) {
            throw new BusinessException(ProductionErrorCode.LOT_NOT_PASSED);
        }

        if (request.defectQty() != null && request.defectQty() > request.producedQty()) {
            throw new BusinessException(ProductionErrorCode.DEFECT_EXCEEDS_PRODUCED);
        }

        Equipment equipment = (request.equipmentId() != null)
                ? findEquipmentEntityById(request.equipmentId())
                : null;

        ProductionLog log = ProductionLog.builder()
                .lot(lot)
                .processName(request.processName())
                .equipment(equipment)
                .producedQty(request.producedQty())
                .defectQty(request.defectQty())
                .startedAt(request.startedAt())
                .endedAt(request.endedAt())
                .build();
        return ProductionLogResponse.from(productionLogRepository.save(log));
    }

    public List<ProductionLogResponse> getAllProductionLogs() {
        return productionLogRepository.findAll().stream()
                .map(ProductionLogResponse::from)
                .toList();
    }

    public List<ProductionLogResponse> getTodayProductionLogs() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return productionLogRepository.findTodayLogs(startOfDay).stream()
                .map(ProductionLogResponse::from)
                .toList();
    }

    // ── AlarmLog ──────────────────────────────────────────────────

    // 설비에서 발생한 오류나 경고를 알람 로그로 등록합니다.
    @Transactional
    public AlarmResponse createAlarm(AlarmRequest request) {
        Equipment equipment = findEquipmentEntityById(request.equipmentId());
        AlarmLog alarm = AlarmLog.builder()
                .equipment(equipment)
                .alarmCode(request.alarmCode())
                .message(request.message())
                .severity(request.severity())
                .build();
        return AlarmResponse.from(alarmLogRepository.save(alarm));
    }

    public List<AlarmResponse> getAllAlarms() {
        return alarmLogRepository.findAll().stream()
                .map(AlarmResponse::from)
                .toList();
    }

    public List<AlarmResponse> getActiveAlarms() {
        return alarmLogRepository.findByResolvedAtIsNullOrderByOccurredAtDesc().stream()
                .map(AlarmResponse::from)
                .toList();
    }

    // 현재 진행 중인 알람 내역을 조치 완료 상태(Resolved)로 처리합니다.
    @Transactional
    public AlarmResponse resolveAlarm(Long id) {
        AlarmLog alarm = alarmLogRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ProductionErrorCode.ALARM_NOT_FOUND));

        if (alarm.isResolved()) {
            throw new BusinessException(ProductionErrorCode.ALARM_ALREADY_RESOLVED);
        }

        alarm.resolve();
        return AlarmResponse.from(alarm);
    }
}