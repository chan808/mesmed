package com.chan.medmes.inspection.error;

import com.chan.medmes.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InspectionErrorCode implements ErrorCode {

    INSPECTION_NOT_FOUND(404, "검사 기록을 찾을 수 없습니다."),
    SPEC_NOT_FOUND(404, "검사기준을 찾을 수 없습니다.");

    private final int status;
    private final String message;
}
