package com.chan.medmes.production.error;

import com.chan.medmes.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProductionErrorCode implements ErrorCode {

    EQUIPMENT_NOT_FOUND(404, "설비를 찾을 수 없습니다."),
    EQUIPMENT_CODE_DUPLICATED(409, "이미 존재하는 설비 코드입니다."),
    ALARM_NOT_FOUND(404, "알람을 찾을 수 없습니다."),
    ALARM_ALREADY_RESOLVED(400, "이미 해소된 알람입니다."),
    PRODUCTION_LOG_NOT_FOUND(404, "생산 이력을 찾을 수 없습니다."),
    LOT_NOT_PASSED(400, "수입검사를 통과한 LOT만 생산 투입이 가능합니다.");

    private final int status;
    private final String message;
}