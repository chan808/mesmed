package com.chan.medmes.material.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record LotRequest(
        @NotNull(message = "원자재 ID는 필수입니다.")
        Long rawMaterialId,

        @Size(max = 50)
        String lotNo,               // null이면 자동 생성 (LOT-YYYYMMDD-NNN)

        @NotNull(message = "수량은 필수입니다.")
        @Positive(message = "수량은 양수여야 합니다.")
        Integer quantity,

        LocalDateTime receivedAt,   // null이면 현재 시간

        @Size(max = 100)
        String supplier
) {}