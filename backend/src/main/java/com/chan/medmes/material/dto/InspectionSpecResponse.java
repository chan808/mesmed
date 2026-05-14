package com.chan.medmes.material.dto;

import com.chan.medmes.material.entity.InspectionSpec;
import com.chan.medmes.material.enums.InspectionCategory;
import com.chan.medmes.material.enums.InspectionEquipment;
import com.chan.medmes.material.enums.InspectionMethod;
import com.chan.medmes.material.enums.InspectionTiming;
import com.chan.medmes.material.enums.MeasureType;

public record InspectionSpecResponse(
        Long id,
        Long rawMaterialId,
        String rawMaterialName,
        InspectionCategory category,
        String itemName,
        String specDesc,
        InspectionMethod method,
        String methodCustom,
        InspectionEquipment equipment,
        String equipmentCustom,
        InspectionTiming timing,
        String timingCustom,
        MeasureType measureType,
        Double minValue,
        Double maxValue,
        String unit,
        int version
) {
    public static InspectionSpecResponse from(InspectionSpec s) {
        return new InspectionSpecResponse(
                s.getId(),
                s.getRawMaterial().getId(),
                s.getRawMaterial().getName(),
                s.getCategory(),
                s.getItemName(), s.getSpecDesc(),
                s.getMethod(), s.getMethodCustom(),
                s.getEquipment(), s.getEquipmentCustom(),
                s.getTiming(), s.getTimingCustom(),
                s.getMeasureType(), s.getMinValue(), s.getMaxValue(), s.getUnit(),
                s.getVersion()
        );
    }
}
