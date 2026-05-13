package com.chan.medmes.production.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProductionLogRequest(
        @NotNull(message = "LOT ID는 필수입니다.")
        Long lotId,

        @NotNull(message = "공정명은 필수입니다.")
        @Size(max = 100)
        String processName,

        Long equipmentId,   // null 허용

        @NotNull(message = "생산 수량은 필수입니다.")
        @Positive(message = "생산 수량은 양수여야 합니다.")
        Integer producedQty,

        @Min(value = 0, message = "불량 수량은 0 이상이어야 합니다.")
        Integer defectQty,

        LocalDateTime startedAt,
        LocalDateTime endedAt
) {}