package com.chan.medmes.material;

import com.chan.medmes.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MaterialErrorCode implements ErrorCode {

    MATERIAL_NOT_FOUND(404, "원자재를 찾을 수 없습니다."),
    MATERIAL_CODE_DUPLICATED(409, "이미 존재하는 원자재 코드입니다."),
    LOT_NOT_FOUND(404, "LOT를 찾을 수 없습니다."),
    LOT_NO_DUPLICATED(409, "이미 존재하는 LOT 번호입니다."),
    SPEC_NOT_FOUND(404, "검사기준을 찾을 수 없습니다."),
    NO_INSPECTION_SPEC(400, "등록된 검사기준이 없습니다. 검사기준을 먼저 등록해주세요.");

    private final int status;
    private final String message;
}
