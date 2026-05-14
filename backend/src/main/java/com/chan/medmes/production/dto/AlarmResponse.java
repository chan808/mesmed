package com.chan.medmes.production.dto;

import com.chan.medmes.production.entity.AlarmLog;
import com.chan.medmes.production.enums.AlarmSeverity;

import java.time.LocalDateTime;

public record AlarmResponse(
        Long id,
        Long equipmentId,
        String equipmentName,
        String alarmCode,
        String message,
        AlarmSeverity severity,
        LocalDateTime occurredAt,
        LocalDateTime resolvedAt,
        boolean resolved
) {
    public static AlarmResponse from(AlarmLog log) {
        return new AlarmResponse(
                log.getId(),
                log.getEquipment().getId(),
                log.getEquipment().getName(),
                log.getAlarmCode(),
                log.getMessage(),
                log.getSeverity(),
                log.getOccurredAt(),
                log.getResolvedAt(),
                log.isResolved()
        );
    }
}