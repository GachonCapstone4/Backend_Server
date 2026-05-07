package com.emailagent.config;

import com.emailagent.domain.entity.User;
import com.emailagent.domain.enums.UserRole;
import com.emailagent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("[AdminInitializer] 관리자 계정이 이미 존재합니다 — email={}", adminEmail);
            return;
        }

        User admin = User.builder()
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .name("관리자")
                .role(UserRole.ADMIN)
                .isActive(true)
                .onboardingCompleted(true)
                .build();

        userRepository.save(admin);
        log.info("[AdminInitializer] 관리자 계정 생성 완료 — email={}", adminEmail);
    }
}
