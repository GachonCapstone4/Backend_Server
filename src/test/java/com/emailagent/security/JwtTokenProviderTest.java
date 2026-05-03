package com.emailagent.security;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET =
            "test-secret-key-for-jwt-token-provider-that-is-long-enough-for-hs256";

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
            SECRET,
            86_400_000L,
            604_800_000L
    );

    @Test
    void signupStateKeepsModeAndFrontendOrigin() {
        String token = jwtTokenProvider.generateOAuthStateTokenForSignup(
                "https://capstone.studylink.click"
        );

        assertThat(jwtTokenProvider.getOAuthStateMode(token)).isEqualTo("SIGNUP");
        assertThat(jwtTokenProvider.getOAuthStateFrontendOrigin(token))
                .isEqualTo(Optional.of("https://capstone.studylink.click"));
    }

    @Test
    void integrationStateKeepsUserAndFrontendOrigin() {
        String token = jwtTokenProvider.generateOAuthStateToken(
                42L,
                "https://mymaily.vercel.app"
        );

        assertThat(jwtTokenProvider.getOAuthStateMode(token)).isEqualTo("INTEGRATION");
        assertThat(jwtTokenProvider.getOAuthStateUserId(token)).isEqualTo(42L);
        assertThat(jwtTokenProvider.getOAuthStateFrontendOrigin(token))
                .isEqualTo(Optional.of("https://mymaily.vercel.app"));
    }

    @Test
    void legacyStateWithoutFrontendOriginStillWorks() {
        String token = jwtTokenProvider.generateOAuthStateTokenForSignup();

        assertThat(jwtTokenProvider.getOAuthStateMode(token)).isEqualTo("SIGNUP");
        assertThat(jwtTokenProvider.getOAuthStateFrontendOrigin(token)).isEmpty();
    }
}
