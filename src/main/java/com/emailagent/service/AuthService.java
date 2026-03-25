package com.emailagent.service;

import com.emailagent.domain.entity.User;
import com.emailagent.dto.request.auth.LoginRequest;
import com.emailagent.dto.request.auth.SignupRequest;
import com.emailagent.dto.response.auth.SignupResponse;
import com.emailagent.dto.response.auth.TokenLoginResponse;
import com.emailagent.repository.UserRepository;
import com.emailagent.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private long jwtExpiration; // ms 단위

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .build();
        user = userRepository.save(user);

        return new SignupResponse(user);
    }

    /**
     * POST /api/auth/tokens — 로그인, JWT Access Token 발급
     */
    @Transactional
    public TokenLoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        if (!user.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }

        user.updateLastLogin();

        String accessToken = jwtTokenProvider.generateAccessToken(user.getUserId(), user.getEmail());
        return new TokenLoginResponse(accessToken, jwtExpiration);
    }
}
