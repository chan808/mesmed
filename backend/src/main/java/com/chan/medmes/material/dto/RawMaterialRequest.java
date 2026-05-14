package com.chan.medmes.material.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RawMaterialRequest (
        @NotBlank(message = "원자재 코드는 필수입니다.")
        @Size(max = 50)
        String code,

        @NotBlank(message = "원자재명은 필수입니다.")
        @Size(max = 100)
        String name,

        @Size(max = 50)
        String category,

        @Size(max = 20)
        String unit,

        @Size(max = 200)
        String specStandard
){
}
