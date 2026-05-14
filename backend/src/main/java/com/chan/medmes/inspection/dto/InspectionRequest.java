package com.chan.medmes.inspection.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record InspectionRequest(
        @NotNull(message = "LOT ID는 필수입니다.")
        Long lotId,

        Long inspectorId,   // null 허용

        String note,

        @NotEmpty(message = "검사 항목은 하나 이상이어야 합니다.")
        @Valid
        List<InspectionDetailItem> details
) {}
