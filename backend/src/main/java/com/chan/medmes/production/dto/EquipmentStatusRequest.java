package com.chan.medmes.production.dto;

import com.chan.medmes.production.enums.EquipmentStatus;
import jakarta.validation.constraints.NotNull;

public record EquipmentStatusRequest(
        @NotNull(message = "설비 상태는 필수입니다.")
        EquipmentStatus status
) {}