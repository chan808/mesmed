package com.chan.medmes.material.dto;

import com.chan.medmes.material.entity.InspectionSpec;

public record InspectionSpecResponse(
        Long id,
        Long rawMaterialId,
        String rawMaterialName,
        String itemName,
        String specDesc,
        String method,
        String equipment,
        String timing,
        int version
) {
    public static InspectionSpecResponse from(InspectionSpec s) {
        return new InspectionSpecResponse(
                s.getId(),
                s.getRawMaterial().getId(),
                s.getRawMaterial().getName(),
                s.getItemName(), s.getSpecDesc(),
                s.getMethod(), s.getEquipment(), s.getTiming(),
                s.getVersion()
        );
    }
}