package com.emailagent.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class JwtTokenProvider {

    private final Key key;
    private final long expiration;
    private final long refreshExpiration;
    private static final String OAUTH_STATE_PURPOSE = "google_oauth_state";
    private static final String FRONTEND_ORIGIN_CLAIM = "frontend_origin";

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
        this.refreshExpiration = refreshExpiration;
    }

    public String generateAccessToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * Google OAuth CSRF 방지용 state JWT 생성 (만료: 10분)
     * payload: userId + purpose = "google_oauth_state"
     */
    public String generateOAuthStateToken(Long userId) {
        return generateOAuthStateToken(userId, null);
    }

    public String generateOAuthStateToken(Long userId, String frontendOrigin) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("purpose", OAUTH_STATE_PURPOSE)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000L)); // 10분

        if (frontendOrigin != null && !frontendOrigin.isBlank()) {
            builder.claim(FRONTEND_ORIGIN_CLAIM, frontendOrigin);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateOAuthStateTokenForSignup() {
        return generateOAuthStateTokenForSignup(null);
    }

    public String generateOAuthStateTokenForSignup(String frontendOrigin) {
        JwtBuilder builder = Jwts.builder()
                .setSubject("signup")
                .claim("purpose", OAUTH_STATE_PURPOSE)
                .claim("mode", "SIGNUP")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 10 * 60 * 1000L));

        if (frontendOrigin != null && !frontendOrigin.isBlank()) {
            builder.claim(FRONTEND_ORIGIN_CLAIM, frontendOrigin);
        }

        return builder
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * OAuth state JWT에서 mode 반환 ("SIGNUP" or "INTEGRATION")
     * purpose 클레임이 올바르지 않으면 예외
     */
    public String getOAuthStateMode(String token) {
        Claims claims = getClaims(token);
        String purpose = claims.get("purpose", String.class);
        if (!OAUTH_STATE_PURPOSE.equals(purpose)) {
            throw new io.jsonwebtoken.JwtException("유효하지 않은 OAuth state 토큰입니다.");
        }
        return "SIGNUP".equals(claims.get("mode", String.class)) ? "SIGNUP" : "INTEGRATION";
    }

    public Optional<String> getOAuthStateFrontendOrigin(String token) {
        Claims claims = getClaims(token);
        String purpose = claims.get("purpose", String.class);
        if (!OAUTH_STATE_PURPOSE.equals(purpose)) {
            throw new io.jsonwebtoken.JwtException("유효하지 않은 OAuth state 토큰입니다.");
        }

        String frontendOrigin = claims.get(FRONTEND_ORIGIN_CLAIM, String.class);
        return frontendOrigin == null || frontendOrigin.isBlank()
                ? Optional.empty()
                : Optional.of(frontendOrigin);
    }

    /**
     * OAuth state JWT 검증 후 userId 반환 (INTEGRATION 모드 전용)
     * purpose 클레임이 "google_oauth_state"가 아니면 예외
     */
    public Long getOAuthStateUserId(String token) {
        Claims claims = getClaims(token);
        String purpose = claims.get("purpose", String.class);
        if (!OAUTH_STATE_PURPOSE.equals(purpose)) {
            throw new io.jsonwebtoken.JwtException("유효하지 않은 OAuth state 토큰입니다.");
        }
        return Long.parseLong(claims.getSubject());
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("유효하지 않은 JWT 토큰: {}", e.getMessage());
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
