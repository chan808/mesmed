package com.chan.medmes.inspection.dto;

import com.chan.medmes.inspection.enums.InspectionResult;
import jakarta.validation.constraints.NotNull;

public record InspectionDetailItem(
        @NotNull(message = "검사기준 ID는 필수입니다.")
        Long inspectionSpecId,

        String measuredValue,

        @NotNull(message = "검사 결과는 필수입니다.")
        InspectionResult result
) {}
