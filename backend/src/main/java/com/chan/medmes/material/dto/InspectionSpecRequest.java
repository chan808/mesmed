package com.chan.medmes.material.dto;

import com.chan.medmes.material.enums.MeasureType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InspectionSpecRequest(
        @NotNull(message = "원자재 ID는 필수입니다.")
        Long rawMaterialId,

        @NotBlank(message = "검사항목명은 필수입니다.")
        @Size(max = 100)
        String itemName,

        @Size(max = 300)
        String specDesc,

        @Size(max = 50)
        String method,

        @Size(max = 100)
        String equipment,

        @Size(max = 50)
        String timing,

        MeasureType measureType,
        Double minValue,
        Double maxValue,

        @Size(max = 20)
        String unit
) {}