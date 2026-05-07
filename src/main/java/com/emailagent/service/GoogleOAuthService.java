package com.emailagent.service;

import com.emailagent.domain.entity.GooglePendingRegistration;
import com.emailagent.domain.entity.Integration;
import com.emailagent.domain.entity.User;
import com.emailagent.domain.enums.SyncStatus;
import com.emailagent.dto.request.auth.GoogleSignupRequest;
import com.emailagent.dto.request.auth.IntegrationStatusUpdateRequest;
import com.emailagent.dto.response.auth.*;
import com.emailagent.exception.InsufficientScopeException;
import com.emailagent.repository.GooglePendingRegistrationRepository;
import com.emailagent.repository.IntegrationRepository;
import com.emailagent.repository.UserRepository;
import com.emailagent.security.JwtTokenProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.WatchRequest;
import com.google.api.services.gmail.model.WatchResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthService {

    // Google OAuth 요청 시 포함할 전체 스코프 (Gmail 필수 + Calendar 선택)
    private static final List<String> REQUEST_SCOPES = Arrays.asList(
            "openid",
            "email",
            "profile",
            "https://www.googleapis.com/auth/gmail.readonly",
            "https://www.googleapis.com/auth/gmail.send",
            "https://www.googleapis.com/auth/calendar.events"
    );

    // 콜백 시 반드시 부여되어야 하는 필수 스코프 (없으면 연동 실패 처리)
    private static final String GMAIL_READ_SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private static final String GMAIL_SEND_SCOPE = "https://www.googleapis.com/auth/gmail.send";

    // 선택 스코프 — 사용자가 거부해도 연동은 성공 (is_calendar_connected=false)
    private static final String CALENDAR_EVENTS_SCOPE = "https://www.googleapis.com/auth/calendar.events";
    private static final String LEGACY_CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar";

    // 회원가입 임시 저장 TTL (10분)
    private static final long PENDING_TTL_MINUTES = 10;
    private static final Set<String> LOOPBACK_FRONTEND_HOSTS = Set.of("localhost", "127.0.0.1", "::1");

    private final IntegrationRepository integrationRepository;
    private final UserRepository userRepository;
    private final GooglePendingRegistrationRepository pendingRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleApiClientProvider googleApiClientProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserDataCleanupService userDataCleanupService;

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    @Value("${app.google.redirect-uri}")
    private String redirectUri;

    @Value("${app.google.topic-name}")
    private String topicName;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Value("#{'${app.frontend.allowed-origins:https://capstone.studylink.click}'.split(',')}")
    private List<String> allowedFrontendOrigins;

    // ── 1. Google OAuth 인증 URL 생성 ──────────────────────────────────────────

    /**
     * 기존 로그인 유저의 Gmail 연동용 OAuth URL 생성.
     * state JWT에 userId 포함 (mode=INTEGRATION).
     */
    @Transactional(readOnly = true)
    public AuthorizationUrlResponse getAuthorizationUrl(Long userId) {
        return getAuthorizationUrl(userId, null);
    }

    @Transactional(readOnly = true)
    public AuthorizationUrlResponse getAuthorizationUrl(Long userId, String frontendOrigin) {
        String stateJwt = jwtTokenProvider.generateOAuthStateToken(
                userId,
                resolveFrontendOrigin(frontendOrigin));
        String url = buildOAuthUrl(stateJwt);
        return new AuthorizationUrlResponse(url);
    }

    /**
     * 비로그인 신규 유저의 Google 회원가입용 OAuth URL 생성.
     * state JWT에 mode=SIGNUP만 포함 (userId 없음).
     */
    public AuthorizationUrlResponse getSignupAuthorizationUrl() {
        return getSignupAuthorizationUrl(null);
    }

    public AuthorizationUrlResponse getSignupAuthorizationUrl(String frontendOrigin) {
        String stateJwt = jwtTokenProvider.generateOAuthStateTokenForSignup(
                resolveFrontendOrigin(frontendOrigin));
        String url = buildOAuthUrl(stateJwt);
        return new AuthorizationUrlResponse(url);
    }

    // ── 2. 콜백 처리 (mode 분기) ───────────────────────────────────────────────

    /**
     * Google OAuth 콜백 공통 진입점.
     * state JWT의 mode 클레임으로 SIGNUP / INTEGRATION 분기.
     */
    @Transactional
    public OAuthCallbackResult handleCallback(String code, String state) throws IOException {
        String mode = jwtTokenProvider.getOAuthStateMode(state);
        if ("SIGNUP".equals(mode)) {
            return processSignupCallback(code);
        }
        Long userId = jwtTokenProvider.getOAuthStateUserId(state);
        return processIntegrationCallback(code, userId);
    }

    /**
     * INTEGRATION 모드 — 이미 로그인한 유저의 Gmail 연동 처리.
     * 1) Code → Token 교환
     * 2) Gmail scope 검증
     * 3) Integrations upsert + watch() 등록
     */
    private OAuthCallbackResult processIntegrationCallback(String code, Long userId) throws IOException {
        GoogleTokenResponse tokenResponse = exchangeCode(code);

        String grantedScopesRaw = tokenResponse.getScope();
        List<String> grantedScopes = Arrays.asList(grantedScopesRaw.split(" "));
        boolean isGmailConnected = hasRequiredGmailScopes(grantedScopes);
        boolean isCalendarConnected = hasCalendarScope(grantedScopes);

        if (!isGmailConnected) {
            throw new InsufficientScopeException("필수 메일 권한이 누락되었습니다. 다시 동의해 주세요.");
        }

        GoogleIdToken.Payload payload = tokenResponse.parseIdToken().getPayload();
        String connectedEmail = payload.getEmail();
        String externalAccountId = payload.getSubject();
        LocalDateTime tokenExpiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds());

        final boolean gmailFlag = isGmailConnected;
        final boolean calendarFlag = isCalendarConnected;

        Integration savedIntegration = integrationRepository.findByUser_UserId(userId)
                .map(existing -> {
                    existing.updateTokens(
                            tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(),
                            tokenExpiresAt, grantedScopesRaw, connectedEmail,
                            externalAccountId, gmailFlag, calendarFlag);
                    return existing;
                })
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
                    return integrationRepository.save(Integration.builder()
                            .user(user)
                            .connectedEmail(connectedEmail)
                            .externalAccountId(externalAccountId)
                            .accessToken(tokenResponse.getAccessToken())
                            .refreshToken(tokenResponse.getRefreshToken())
                            .tokenExpiresAt(tokenExpiresAt)
                            .grantedScopes(grantedScopesRaw)
                            .isGmailConnected(true)
                            .isCalendarConnected(calendarFlag)
                            .syncStatus(SyncStatus.CONNECTED)
                            .lastSyncedAt(LocalDateTime.now())
                            .build());
                });

        registerWatch(savedIntegration);
        return OAuthCallbackResult.integrationDone(isGmailConnected, isCalendarConnected);
    }

    /**
     * SIGNUP 모드 — 비로그인 유저의 Google 회원가입 처리.
     *
     * [Case 1] Gmail이 Integrations에 이미 존재
     *   → 이미 가입된 계정이므로 회원가입 중단.
     *
     * [Case 2] Gmail이 Users에만 존재 (일반 가입, 미연동)
     *   → 이미 가입된 계정이므로 회원가입 중단.
     *
     * [Case 3] Gmail이 어디에도 없음 (신규)
     *   → OAuth 데이터 임시 저장, 프론트 회원가입 페이지로 redirect.
     */
    private OAuthCallbackResult processSignupCallback(String code) throws IOException {
        GoogleTokenResponse tokenResponse = exchangeCode(code);

        String grantedScopesRaw = tokenResponse.getScope();
        List<String> grantedScopes = Arrays.asList(grantedScopesRaw.split(" "));
        boolean isCalendarConnected = hasCalendarScope(grantedScopes);

        if (!hasRequiredGmailScopes(grantedScopes)) {
            throw new InsufficientScopeException("필수 메일 권한이 누락되었습니다. 다시 동의해 주세요.");
        }

        GoogleIdToken.Payload payload = tokenResponse.parseIdToken().getPayload();
        String gmailAddress = payload.getEmail();
        String name = payload.get("name") != null ? (String) payload.get("name") : "";
        String externalAccountId = payload.getSubject();
        LocalDateTime tokenExpiresAt = LocalDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds());

        // Case 1: Integration에 이미 있고 해당 유저가 활성 상태 → 회원가입 중단
        Optional<Integration> existingIntegration = integrationRepository.findByConnectedEmail(gmailAddress);
        if (existingIntegration.isPresent() && existingIntegration.get().getUser().isActive()) {
            log.info("[Google 회원가입] 이미 연동된 활성 계정으로 회원가입 시도: email={}", gmailAddress);
            throw new IllegalStateException("이미 가입된 회원입니다. 로그인 화면에서 로그인해 주세요.");
        }

        // Case 2: Users에만 있고 활성 상태 → 회원가입 중단 (탈퇴 계정은 재가입 허용)
        Optional<User> existingUser = userRepository.findByEmail(gmailAddress);
        if (existingUser.isPresent() && existingUser.get().isActive()) {
            log.info("[Google 회원가입] 이미 가입된 이메일로 회원가입 시도: email={}", gmailAddress);
            throw new IllegalStateException("이미 가입된 회원입니다. 로그인 화면에서 로그인해 주세요.");
        }

        // Case 3: 신규 유저 → OAuth 데이터 DB에 임시 저장 후 회원가입 페이지로
        // 멀티 파드 환경에서도 어느 파드에서 Step 2 요청을 받아도 조회 가능하도록 DB에 저장한다.
        String tempToken = UUID.randomUUID().toString();
        pendingRepository.save(GooglePendingRegistration.builder()
                .tempToken(tempToken)
                .gmailAddress(gmailAddress)
                .name(name)
                .accessToken(tokenResponse.getAccessToken())
                .refreshToken(tokenResponse.getRefreshToken())
                .tokenExpiresAt(tokenExpiresAt)
                .grantedScopes(grantedScopesRaw)
                .externalAccountId(externalAccountId)
                .isCalendarConnected(isCalendarConnected)
                .expiresAt(LocalDateTime.now().plusMinutes(PENDING_TTL_MINUTES))
                .build());
        log.info("[Google 회원가입] 신규 유저 임시 저장 (DB): email={}", gmailAddress);
        return OAuthCallbackResult.pendingRegistration(tempToken, gmailAddress, name);
    }

    // ── 3. 회원가입 완료 (Step 2 — 비밀번호 입력 후) ──────────────────────────

    /**
     * Google 회원가입 Step 2: temp_token 검증 + 비밀번호 수신 → User + Integration 생성.
     * temp_token은 검증 성공 즉시 삭제(재사용 방지).
     */
    @Transactional
    public TokenLoginResponse completeGoogleSignup(GoogleSignupRequest request) {
        // temp_token으로 DB에서 임시 저장 데이터 조회
        GooglePendingRegistration pending = pendingRepository.findById(request.getTempToken())
                .orElseThrow(() -> new IllegalArgumentException("회원가입 세션이 만료되었습니다. 다시 시도해 주세요."));

        // TTL 검증
        if (LocalDateTime.now().isAfter(pending.getExpiresAt())) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException("회원가입 세션이 만료되었습니다. 다시 시도해 주세요.");
        }

        LocalDateTime tokenExpiresAt = pending.getTokenExpiresAt();

        // 동일 이메일이 이미 존재하면 가입 불가
        if (userRepository.findByEmail(pending.getGmailAddress()).isPresent()) {
            pendingRepository.delete(pending);
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 신규 가입
        User user = userRepository.save(User.builder()
                .email(pending.getGmailAddress())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(pending.getName())
                .build());

        Integration integration = integrationRepository.save(Integration.builder()
                .user(user)
                .connectedEmail(pending.getGmailAddress())
                .externalAccountId(pending.getExternalAccountId())
                .accessToken(pending.getAccessToken())
                .refreshToken(pending.getRefreshToken())
                .tokenExpiresAt(tokenExpiresAt)
                .grantedScopes(pending.getGrantedScopes())
                .isGmailConnected(true)
                .isCalendarConnected(pending.isCalendarConnected())
                .syncStatus(SyncStatus.CONNECTED)
                .lastSyncedAt(LocalDateTime.now())
                .build());

        registerWatch(integration);
        pendingRepository.delete(pending);

        log.info("[Google 회원가입] 신규 계정 생성 완료: userId={}, email={}", user.getUserId(), user.getEmail());
        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getEmail());
        return new TokenLoginResponse(accessToken, jwtExpiration);
    }

    // ── 4. 연동 정보 조회 ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public IntegrationResponse getMyIntegration(Long userId) {
        Integration integration = findIntegration(userId);
        return new IntegrationResponse(integration);
    }

    // ── 5. 연동 상태 변경 ──────────────────────────────────────────────────────

    @Transactional
    public IntegrationStatusResponse updateStatus(Long userId, IntegrationStatusUpdateRequest request) {
        Integration integration = findIntegration(userId);
        integration.updateSyncStatus(request.getSyncStatus());
        return new IntegrationStatusResponse(integration);
    }

    // ── 6. 연동 해제 ───────────────────────────────────────────────────────────

    @Transactional
    public BaseResponse deleteIntegration(Long userId) {
        Integration integration = findIntegration(userId);
        stopGmailWatch(integration);
        integrationRepository.delete(integration);
        return new BaseResponse();
    }

    /**
     * 관리자 강제 해제 등 외부 호출용 — userId로 watch 중단.
     * Integration이 없으면 조용히 무시 (이미 해제된 경우 대비).
     */
    public void stopGmailWatchIfPresent(Long userId) {
        integrationRepository.findByUser_UserId(userId).ifPresent(this::stopGmailWatch);
    }

    // ── 내부 헬퍼 ──────────────────────────────────────────────────────────────

    private String buildOAuthUrl(String stateJwt) {
        return new GoogleAuthorizationCodeRequestUrl(clientId, redirectUri, REQUEST_SCOPES)
                .setAccessType("offline")
                .set("prompt", "consent")
                .setState(stateJwt)
                .build();
    }

    private String resolveFrontendOrigin(String requestedOrigin) {
        return normalizeOrigin(requestedOrigin)
                .filter(this::isAllowedFrontendOrigin)
                .or(() -> normalizeOrigin(frontendBaseUrl))
                .orElse(frontendBaseUrl);
    }

    private boolean isAllowedFrontendOrigin(String origin) {
        if (normalizeOrigin(frontendBaseUrl).filter(origin::equals).isPresent()) {
            return true;
        }

        boolean listed = allowedFrontendOrigins.stream()
                .map(this::normalizeOrigin)
                .flatMap(Optional::stream)
                .anyMatch(origin::equals);

        if (listed) {
            return true;
        }

        try {
            URI uri = new URI(origin);
            String host = uri.getHost();
            return "http".equals(uri.getScheme())
                    && host != null
                    && LOOPBACK_FRONTEND_HOSTS.contains(host)
                    && uri.getPort() > 0;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private Optional<String> normalizeOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return Optional.empty();
        }

        try {
            URI uri = new URI(origin.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) {
                return Optional.empty();
            }

            if (!"http".equals(scheme) && !"https".equals(scheme)) {
                return Optional.empty();
            }

            int port = uri.getPort();
            String normalized = scheme + "://" + host.toLowerCase();
            if (port != -1) {
                normalized += ":" + port;
            }
            return Optional.of(normalized);
        } catch (URISyntaxException e) {
            return Optional.empty();
        }
    }

    private GoogleTokenResponse exchangeCode(String code) throws IOException {
        return new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                code,
                redirectUri
        ).execute();
    }

    private boolean hasRequiredGmailScopes(List<String> grantedScopes) {
        return grantedScopes.contains(GMAIL_READ_SCOPE) && grantedScopes.contains(GMAIL_SEND_SCOPE);
    }

    private boolean hasCalendarScope(List<String> grantedScopes) {
        return grantedScopes.contains(CALENDAR_EVENTS_SCOPE) || grantedScopes.contains(LEGACY_CALENDAR_SCOPE);
    }

    /**
     * Gmail watch() 등록 — Pub/Sub Push 알림 수신 구독.
     * 실패해도 OAuth 연동은 유지되어야 하므로 예외를 삼키고 로그만 남긴다.
     */
    private void registerWatch(Integration integration) {
        try {
            Gmail gmailClient = googleApiClientProvider.buildGmailClient(integration);
            WatchRequest watchRequest = new WatchRequest()
                    .setTopicName(topicName)
                    .setLabelIds(List.of("INBOX"));
            WatchResponse watchResponse = gmailClient.users().watch("me", watchRequest).execute();
            integration.updateLastHistoryId(watchResponse.getHistoryId().longValue());
            log.info("[OAuth] Gmail watch() 등록 완료 — userId={}, historyId={}, expiration={}",
                    integration.getUser().getUserId(), watchResponse.getHistoryId(), watchResponse.getExpiration());
        } catch (Exception e) {
            log.error("[OAuth] Gmail watch() 등록 실패 — userId={}, error={}",
                    integration.getUser().getUserId(), e.getMessage(), e);
        }
    }

    /**
     * Gmail watch() 중단 — 연동 해제 시 Pub/Sub Push 구독을 즉시 중단한다.
     * 실패해도 연동 해제(DB 삭제)는 계속 진행되어야 하므로 예외를 삼키고 로그만 남긴다.
     * stop() 후에도 Google이 최대 수십 초간 push를 보낼 수 있으므로
     * PubSubHandlerService에서도 연동 없는 메시지를 graceful하게 처리한다.
     */
    private void stopGmailWatch(Integration integration) {
        try {
            Gmail gmailClient = googleApiClientProvider.buildGmailClient(integration);
            gmailClient.users().stop("me").execute();
            log.info("[OAuth] Gmail watch() 중단 완료 — userId={}, email={}",
                    integration.getUser().getUserId(), integration.getConnectedEmail());
        } catch (Exception e) {
            log.warn("[OAuth] Gmail watch() 중단 실패 (연동 해제는 계속 진행) — userId={}, error={}",
                    integration.getUser().getUserId(), e.getMessage());
        }
    }

    private Integration findIntegration(Long userId) {
        return integrationRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("연동 정보가 존재하지 않습니다."));
    }
}
