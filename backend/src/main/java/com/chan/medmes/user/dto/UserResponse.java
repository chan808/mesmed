package com.chan.medmes.user.dto;

import com.chan.medmes.user.enums.UserRole;
import com.chan.medmes.user.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String displayName,
        UserRole role,
        LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
