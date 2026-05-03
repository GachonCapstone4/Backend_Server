package com.emailagent.domain.entity;

import com.emailagent.domain.converter.NotificationConfigConverter;
import com.emailagent.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

    @Column(name = "onboarding_completed", nullable = false)
    @Builder.Default
    private boolean onboardingCompleted = false;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // 비밀번호 재설정 인증 코드 (멀티 파드 환경에서 공유되도록 DB 저장)
    @Column(name = "reset_code", length = 6)
    private String resetCode;

    @Column(name = "reset_code_expires_at")
    private LocalDateTime resetCodeExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 알림 설정 (JSON): {"NEW_EMAIL": true, "DRAFT_PENDING": false, ...}
    // null이면 NotificationConfigConverter가 모든 타입 true인 기본값으로 복원
    @Column(name = "notification_configs", columnDefinition = "JSON")
    @Convert(converter = NotificationConfigConverter.class)
    @Builder.Default
    private Map<String, Boolean> notificationConfigs = NotificationConfigConverter.defaultConfigs();

    // 연관관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Email> emails = new ArrayList<>();

    // 비즈니스 메서드
    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateActiveStatus(boolean isActive) {
        this.isActive = isActive;
    }

    public void completeOnboarding() {
        this.onboardingCompleted = true;
    }

    public void saveResetCode(String code, LocalDateTime expiresAt) {
        this.resetCode = code;
        this.resetCodeExpiresAt = expiresAt;
    }

    public void clearResetCode() {
        this.resetCode = null;
        this.resetCodeExpiresAt = null;
    }

    public void updateNotificationConfigs(Map<String, Boolean> configs) {
        this.notificationConfigs = configs;
    }

    // 탈퇴 계정 재가입: 비밀번호·이름 갱신, 활성화, 온보딩 초기화
    public void reactivate(String encodedPassword, String name) {
        this.isActive = true;
        this.password = encodedPassword;
        this.name = name;
        this.onboardingCompleted = false;
    }
}
