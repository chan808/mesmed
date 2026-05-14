package com.chan.medmes.material.dto;

import com.chan.medmes.material.enums.LotStatus;
import jakarta.validation.constraints.NotNull;

public record LotStatusRequest(
        @NotNull(message = "상태는 필수입니다.")
        LotStatus status
) {}