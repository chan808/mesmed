package com.chan.medmes.production.dto;

import com.chan.medmes.production.enums.AlarmSeverity;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlarmRequest(
        @NotNull(message = "설비 ID는 필수입니다.")
        Long equipmentId,

        @Size(max = 50)
        String alarmCode,

        @Size(max = 500)
        String message,

        @NotNull(message = "심각도는 필수입니다.")
        AlarmSeverity severity
) {}