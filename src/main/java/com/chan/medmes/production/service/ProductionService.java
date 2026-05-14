package com.chan.medmes.production.service;

import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.material.LotStatus;
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