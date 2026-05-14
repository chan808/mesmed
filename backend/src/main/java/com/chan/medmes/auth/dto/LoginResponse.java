package com.chan.medmes.auth.dto;

public record LoginResponse(
        String accessToken,
        Long userId,
        String role
) {}