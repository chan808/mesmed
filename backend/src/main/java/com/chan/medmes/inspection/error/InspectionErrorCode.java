package com.chan.medmes.inspection.error;

import com.chan.medmes.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InspectionErrorCode implements ErrorCode {

    INSPECTION_NOT_FOUND(404, "검사 기록을 찾을 수 없습니다."),
    SPEC_NOT_FOUND(404, "검사기준을 찾을 수 없습니다."),
    SPEC_MATERIAL_MISMATCH(400, "검사기준이 해당 LOT의 원자재에 속하지 않습니다."),
    MEASURED_VALUE_REQUIRED(400, "수치 검사 항목은 measuredValue가 필수입니다."),
    MEASURED_VALUE_NOT_NUMERIC(400, "수치 검사 항목의 measuredValue는 숫자여야 합니다.");

    private final int status;
    private final String message;
}
