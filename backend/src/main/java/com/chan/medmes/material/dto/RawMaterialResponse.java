package com.chan.medmes.material.dto;

import com.chan.medmes.material.entity.RawMaterial;

import java.time.LocalDateTime;

public record RawMaterialResponse(
        Long id,
        String code,
        String name,
        String category,
        String unit,
        String specStandard,
        LocalDateTime createdAt
) {
    public static RawMaterialResponse from(RawMaterial m) {
        return new RawMaterialResponse(
                m.getId(), m.getCode(), m.getName(),
                m.getCategory(), m.getUnit(), m.getSpecStandard(), m.getCreatedAt()
        );
    }
}