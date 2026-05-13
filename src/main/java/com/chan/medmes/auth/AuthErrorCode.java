package com.chan.medmes.auth;

import com.chan.medmes.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    INVALID_CREDENTIALS(401, "아이디 또는 비밀번호가 올바르지 않습니다.");

    private final int status;
    private final String message;
}