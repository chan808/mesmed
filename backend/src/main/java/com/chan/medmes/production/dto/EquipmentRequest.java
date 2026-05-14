package com.chan.medmes.production.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipmentRequest(
        @NotBlank(message = "설비 코드는 필수입니다.")
        @Size(max = 50)
        String equipmentCode,

        @NotBlank(message = "설비명은 필수입니다.")
        @Size(max = 100)
        String name
) {}