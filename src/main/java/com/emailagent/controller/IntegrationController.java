package com.emailagent.controller;

import com.emailagent.dto.request.auth.IntegrationStatusUpdateRequest;
import com.emailagent.dto.response.auth.*;
import com.emailagent.security.CurrentUser;
import com.emailagent.security.JwtTokenProvider;
import com.emailagent.service.GoogleOAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/integrations")
@RequiredArgsConstructor
public class IntegrationController {

    private static final String DESKTOP_CALLBACK_SCHEME = "maily";
    private static final String DESKTOP_CALLBACK_HOST = "app";
    private static final MediaType TEXT_HTML_UTF8 = MediaType.parseMediaType("text/html;charset=UTF-8");

    private final GoogleOAuthService googleOAuthService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    // Google OAuth 팝업 종료를 담당하는 프론트 콜백 경로
    @Value("${app.frontend.oauth-callback-path:/oauth/google/callback}")
    private String oauthCallbackPath;

    @GetMapping("/google/authorization-url")
    public ResponseEntity<AuthorizationUrlResponse> getAuthorizationUrl(
            @CurrentUser Long userId,
            @RequestParam(value = "frontend_origin", required = false) String frontendOrigin) {
        return ResponseEntity.ok(googleOAuthService.getAuthorizationUrl(userId, frontendOrigin));
    }

    /**
     * Google OAuth 콜백 공통 처리.
     * state JWT의 mode로 INTEGRATION / SIGNUP 분기 후 프론트 공통 콜백 경로로 redirect.
     */
    @GetMapping("/google/callback")
    public ResponseEntity<?> handleCallback(
            @RequestParam String code,
            @RequestParam String state) {
        String frontendOrigin = resolveStateFrontendOrigin(state);

        try {
            OAuthCallbackResult result = googleOAuthService.handleCallback(code, state);

            String redirectUrl = switch (result.getType()) {
                case INTEGRATION_DONE -> buildOAuthCallbackUrl(
                        "google_oauth=success"
                                + "&gmail_connected=" + result.isGmailConnected()
                                + "&calendar_connected=" + result.isCalendarConnected(),
                        frontendOrigin
                );

                case AUTO_LOGIN -> buildOAuthCallbackUrl(
                        "google_oauth=auto_login"
                                + "&token=" + encode(result.getJwt()),
                        frontendOrigin
                );

                case PENDING_REGISTRATION -> buildOAuthCallbackUrl(
                        "google_oauth=pending_registration"
                                + "&temp_token=" + encode(result.getTempToken())
                                + "&email=" + encode(result.getEmail())
                                + "&name=" + encode(result.getName()),
                        frontendOrigin
                );
            };

            return buildCallbackResponse(redirectUrl);

        } catch (Exception e) {
            // 어느 모드에서 실패했는지 state JWT로 판별해 적절한 에러 페이지로 redirect
            boolean isSignupMode = false;
            try {
                isSignupMode = "SIGNUP".equals(jwtTokenProvider.getOAuthStateMode(state));
            } catch (Exception ignored) {}

            String message = URLEncoder.encode(
                    e.getMessage() != null ? e.getMessage() : "Google OAuth 처리에 실패했습니다.",
                    StandardCharsets.UTF_8);

            String errorRedirect = buildOAuthCallbackUrl(
                    "google_oauth=error"
                            + "&mode=" + (isSignupMode ? "signup" : "integration")
                            + "&message=" + message,
                    frontendOrigin
            );

            return buildCallbackResponse(errorRedirect);
        }
    }

    private String buildOAuthCallbackUrl(String queryString) {
        return buildOAuthCallbackUrl(queryString, frontendBaseUrl);
    }

    private String buildOAuthCallbackUrl(String queryString, String frontendOrigin) {
        String separator = oauthCallbackPath.contains("?") ? "&" : "?";
        return frontendOrigin + oauthCallbackPath + separator + queryString;
    }

    private ResponseEntity<?> buildCallbackResponse(String callbackUrl) {
        if (isDesktopCallbackUrl(callbackUrl)) {
            return ResponseEntity.ok()
                    .contentType(TEXT_HTML_UTF8)
                    .body(buildDesktopHandoffHtml(callbackUrl));
        }

        return ResponseEntity.status(302)
                .location(URI.create(callbackUrl))
                .build();
    }

    private boolean isDesktopCallbackUrl(String callbackUrl) {
        try {
            URI uri = URI.create(callbackUrl);
            return DESKTOP_CALLBACK_SCHEME.equals(uri.getScheme())
                    && DESKTOP_CALLBACK_HOST.equals(uri.getHost());
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String buildDesktopHandoffHtml(String deepLinkUrl) {
        String escapedDeepLink = escapeHtml(deepLinkUrl);
        String jsDeepLink = escapeJsString(deepLinkUrl);

        return """
                <!doctype html>
                <html lang="ko">
                  <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1">
                    <title>Maily 앱으로 돌아가기</title>
                    <style>
                      :root {
                        color-scheme: light;
                        --ink: #162033;
                        --muted: #6b7688;
                        --line: #dce6ef;
                        --mint: #2dd4bf;
                        --mint-dark: #0f766e;
                        --bg: #f5f8fb;
                      }
                      * { box-sizing: border-box; }
                      body {
                        margin: 0;
                        min-height: 100vh;
                        display: grid;
                        place-items: center;
                        padding: 24px;
                        background:
                          radial-gradient(circle at 20%% 12%%, rgba(45, 212, 191, 0.18), transparent 32%%),
                          linear-gradient(135deg, #f8fbfd 0%%, #eef7f5 100%%);
                        color: var(--ink);
                        font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                      }
                      main {
                        width: min(100%%, 520px);
                        border: 1px solid rgba(220, 230, 239, 0.86);
                        border-radius: 28px;
                        background: rgba(255, 255, 255, 0.92);
                        box-shadow: 0 24px 80px rgba(22, 32, 51, 0.14);
                        padding: 34px;
                        text-align: center;
                      }
                      .mark {
                        width: 56px;
                        height: 56px;
                        display: inline-grid;
                        place-items: center;
                        margin-bottom: 22px;
                        border-radius: 18px;
                        background: linear-gradient(135deg, #0f766e, #2dd4bf);
                        color: white;
                        font-size: 22px;
                        font-weight: 800;
                        letter-spacing: -0.02em;
                      }
                      h1 {
                        margin: 0;
                        font-size: 24px;
                        line-height: 1.28;
                        letter-spacing: -0.01em;
                      }
                      p {
                        margin: 14px 0 0;
                        color: var(--muted);
                        font-size: 14px;
                        line-height: 1.65;
                      }
                      .actions {
                        display: flex;
                        flex-direction: column;
                        gap: 10px;
                        margin-top: 28px;
                      }
                      button, a {
                        width: 100%%;
                        min-height: 48px;
                        display: inline-flex;
                        align-items: center;
                        justify-content: center;
                        border-radius: 14px;
                        font-size: 14px;
                        font-weight: 700;
                        text-decoration: none;
                        cursor: pointer;
                      }
                      button {
                        border: 0;
                        background: linear-gradient(135deg, #0f766e, #14b8a6);
                        color: white;
                        box-shadow: 0 12px 30px rgba(20, 184, 166, 0.28);
                      }
                      a {
                        border: 1px solid var(--line);
                        background: #fff;
                        color: var(--mint-dark);
                      }
                      .hint {
                        min-height: 20px;
                        margin-top: 18px;
                        color: #8b97a8;
                        font-size: 12px;
                      }
                      .is-closing main {
                        opacity: 0.72;
                        transform: translateY(2px);
                        transition: opacity 180ms ease, transform 180ms ease;
                      }
                    </style>
                  </head>
                  <body>
                    <main>
                      <div class="mark">M</div>
                      <h1>Google 인증이 완료되었습니다</h1>
                      <p>
                        이제 Maily 앱에서 비밀번호 설정과 온보딩을 이어갈 수 있습니다.<br>
                        아래 버튼을 누르고 브라우저의 앱 열기 확인 창을 허용해 주세요.
                      </p>
                      <div class="actions">
                        <button id="open-app" type="button">Maily 앱에서 계속하기</button>
                        <a href="%s">앱 열기 다시 시도</a>
                      </div>
                      <div id="hint" class="hint">버튼을 누르면 이 창은 자동으로 닫기를 시도합니다.</div>
                    </main>
                    <script>
                      const deepLink = "%s";
                      const button = document.getElementById("open-app");
                      const hint = document.getElementById("hint");

                      button.addEventListener("click", () => {
                        document.body.classList.add("is-closing");
                        hint.textContent = "Maily 앱을 여는 중입니다. 앱이 열리면 이 창을 닫아도 됩니다.";
                        window.location.href = deepLink;
                        window.setTimeout(() => window.close(), 700);
                      });
                    </script>
                  </body>
                </html>
                """.formatted(escapedDeepLink, jsDeepLink);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private String escapeJsString(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("<", "\\u003C")
                .replace(">", "\\u003E")
                .replace("&", "\\u0026")
                .replace("\u2028", "\\u2028")
                .replace("\u2029", "\\u2029");
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    private String resolveStateFrontendOrigin(String state) {
        try {
            return jwtTokenProvider.getOAuthStateFrontendOrigin(state)
                    .orElse(frontendBaseUrl);
        } catch (Exception e) {
            return frontendBaseUrl;
        }
    }

    @GetMapping("/me")
    public ResponseEntity<IntegrationResponse> getMyIntegration(@CurrentUser Long userId) {
        return ResponseEntity.ok(googleOAuthService.getMyIntegration(userId));
    }

    @PatchMapping("/me/status")
    public ResponseEntity<IntegrationStatusResponse> updateStatus(
            @CurrentUser Long userId,
            @Valid @RequestBody IntegrationStatusUpdateRequest request) {
        return ResponseEntity.ok(googleOAuthService.updateStatus(userId, request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse> deleteIntegration(@CurrentUser Long userId) {
        return ResponseEntity.ok(googleOAuthService.deleteIntegration(userId));
    }
}
