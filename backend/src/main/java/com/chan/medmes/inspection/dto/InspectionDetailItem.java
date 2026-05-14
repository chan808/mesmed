package com.chan.medmes.inspection.dto;

import com.chan.medmes.inspection.enums.InspectionResult;
import com.chan.medmes.inspection.enums.InspectionSeverity;
import jakarta.validation.constraints.NotNull;

public record InspectionDetailItem(
        @NotNull(message = "검사기준 ID는 필수입니다.")
        Long inspectionSpecId,

        String measuredValue,

        // NUMERIC 기준은 서버가 자동 판정하므로 null 허용 — VISUAL 기준은 필수
        InspectionResult result,

        InspectionSeverity severity
) {}
