package com.chan.medmes.production.dto;

import com.chan.medmes.production.entity.Equipment;
import com.chan.medmes.production.enums.EquipmentStatus;

import java.time.LocalDateTime;

public record EquipmentResponse(
        Long id,
        String equipmentCode,
        String name,
        EquipmentStatus status,
        LocalDateTime lastMaintainedAt
) {
    public static EquipmentResponse from(Equipment e) {
        return new EquipmentResponse(
                e.getId(), e.getEquipmentCode(), e.getName(),
                e.getStatus(), e.getLastMaintainedAt()
        );
    }
}