package com.chan.medmes.global;

import com.chan.medmes.user.enums.UserRole;
import com.chan.medmes.user.entity.User;
import com.chan.medmes.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.count() > 0) {
            return;
        }

        userRepository.save(User.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .displayName("관리자")
                .role(UserRole.ADMIN)
                .build());

        log.info("================================================");
        log.info("초기 관리자 계정 생성: admin / admin123");
        log.info("================================================");
    }
}