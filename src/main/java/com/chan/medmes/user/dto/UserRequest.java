package com.chan.medmes.user.dto;

import com.chan.medmes.user.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest (
        @NotBlank(message = "아이디는 필수입니다.")
        @Size(min = 3, max = 50, message = "아이디는 3~50자 사이여야 합니다.")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다.")
        @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password,

        @Size(max = 100)
        String displayName,

        @NotNull(message = "역할은 필수입니다.")
        UserRole role
){
}
