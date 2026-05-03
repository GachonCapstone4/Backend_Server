package com.emailagent.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Google 회원가입 Step 1(OAuth) 완료 후 Step 2(비밀번호 입력) 전까지
 * OAuth 토큰과 사용자 정보를 임시 보관하는 테이블.
 * 멀티 파드 환경에서도 파드 간 데이터 공유가 가능하도록 DB에 저장한다.
 * TTL(expiresAt) 경과 후 만료되며, 회원가입 완료 즉시 레코드를 삭제한다.
 */
@Entity
@Table(name = "google_pending_registrations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class GooglePendingRegistration {

    @Id
    @Column(name = "temp_token", length = 36, nullable = false)
    private String tempToken;

    @Column(name = "gmail", nullable = false, length = 255)
    private String gmailAddress;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "access_token", nullable = false, columnDefinition = "TEXT")
    private String accessToken;

    @Column(name = "refresh_token", columnDefinition = "TEXT")
    private String refreshToken;

    @Column(name = "token_expires_at", nullable = false)
    private LocalDateTime tokenExpiresAt;

    @Column(name = "granted_scopes", columnDefinition = "TEXT")
    private String grantedScopes;

    @Column(name = "external_account_id", length = 255)
    private String externalAccountId;

    @Column(name = "is_calendar_connected", nullable = false)
    @Builder.Default
    private boolean isCalendarConnected = false;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
