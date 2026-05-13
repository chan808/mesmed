package com.chan.medmes.user;


import com.chan.medmes.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    USERNAME_DUPLICATED(409, "이미 사용 중인 아이디입니다");

    private final int status;
    private final String message;
}
