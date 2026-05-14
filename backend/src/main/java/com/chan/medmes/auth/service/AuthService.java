package com.chan.medmes.auth.service;

import com.chan.medmes.auth.AuthErrorCode;
import com.chan.medmes.auth.dto.LoginRequest;
import com.chan.medmes.auth.dto.LoginResponse;
import com.chan.medmes.global.error.BusinessException;
import com.chan.medmes.global.security.JwtProvider;
import com.chan.medmes.user.entity.User;
import com.chan.medmes.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        String token = jwtProvider.generateToken(user.getId(), user.getRole().name());
        return new LoginResponse(token, user.getId(), user.getRole().name());
    }
}