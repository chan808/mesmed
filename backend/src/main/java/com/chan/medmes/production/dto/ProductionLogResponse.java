package com.chan.medmes.production.dto;

import com.chan.medmes.production.entity.ProductionLog;

import java.time.LocalDateTime;

public record ProductionLogResponse(
        Long id,
        Long lotId,
        String lotNo,
        String processName,
        Long equipmentId,
        String equipmentName,
        Integer producedQty,
        Integer defectQty,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    public static ProductionLogResponse from(ProductionLog log) {
        return new ProductionLogResponse(
                log.getId(),
                log.getLot().getId(),
                log.getLot().getLotNo(),
                log.getProcessName(),
                log.getEquipment() != null ? log.getEquipment().getId() : null,
                log.getEquipment() != null ? log.getEquipment().getName() : null,
                log.getProducedQty(),
                log.getDefectQty(),
                log.getStartedAt(),
                log.getEndedAt()
        );
    }
}