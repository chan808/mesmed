package com.chan.medmes.material.dto;

import com.chan.medmes.material.enums.InspectionCategory;
import com.chan.medmes.material.enums.InspectionEquipment;
import com.chan.medmes.material.enums.InspectionMethod;
import com.chan.medmes.material.enums.InspectionTiming;
import com.chan.medmes.material.enums.MeasureType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InspectionSpecRequest(
        @NotNull(message = "원자재 ID는 필수입니다.")
        Long rawMaterialId,

        // null이면 OTHER로 저장
        InspectionCategory category,

        @NotBlank(message = "검사항목명은 필수입니다.")
        @Size(max = 100)
        String itemName,

        @Size(max = 300)
        String specDesc,

        InspectionMethod method,

        @Size(max = 50)
        String methodCustom,

        InspectionEquipment equipment,

        @Size(max = 100)
        String equipmentCustom,

        InspectionTiming timing,

        @Size(max = 50)
        String timingCustom,

        MeasureType measureType,
        Double minValue,
        Double maxValue,

        @Size(max = 20)
        String unit
) {}
