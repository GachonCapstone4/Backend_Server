package com.emailagent.controller;

import com.emailagent.dto.request.auth.LoginRequest;
import com.emailagent.dto.response.auth.AuthMeResponse;
import com.emailagent.dto.response.auth.TokenLoginResponse;
import com.emailagent.security.CurrentUser;
import com.emailagent.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** POST /api/auth/tokens — 로그인, JWT Access Token 발급 */
    @PostMapping("/tokens")
    public ResponseEntity<TokenLoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /** GET /api/auth/me — 현재 인증된 사용자 확인 */
    @GetMapping("/me")
    public ResponseEntity<AuthMeResponse> getAuthMe(@CurrentUser Long userId) {
        return ResponseEntity.ok(new AuthMeResponse(userId));
    }
}
