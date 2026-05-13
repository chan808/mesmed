package com.chan.medmes.material.dto;

import com.chan.medmes.material.LotStatus;
import com.chan.medmes.material.entity.Lot;

import java.time.LocalDateTime;

public record LotResponse(
        Long id,
        String lotNo,
        Long rawMaterialId,
        String rawMaterialCode,
        String rawMaterialName,
        Integer quantity,
        LocalDateTime receivedAt,
        String supplier,
        LotStatus status
) {
    public static LotResponse from(Lot lot) {
        return new LotResponse(
                lot.getId(), lot.getLotNo(),
                lot.getRawMaterial().getId(),
                lot.getRawMaterial().getCode(),
                lot.getRawMaterial().getName(),
                lot.getQuantity(), lot.getReceivedAt(),
                lot.getSupplier(), lot.getStatus()
        );
    }
}