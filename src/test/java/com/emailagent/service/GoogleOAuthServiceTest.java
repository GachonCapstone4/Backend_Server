package com.emailagent.service;

import com.emailagent.repository.GooglePendingRegistrationRepository;
import com.emailagent.repository.IntegrationRepository;
import com.emailagent.repository.UserRepository;
import com.emailagent.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class GoogleOAuthServiceTest {

    private GoogleOAuthService createServiceWithAllowedOrigins(List<String> allowedOrigins) {
        GoogleOAuthService service = new GoogleOAuthService(
                mock(IntegrationRepository.class),
                mock(UserRepository.class),
                mock(GooglePendingRegistrationRepository.class),
                mock(JwtTokenProvider.class),
                mock(GoogleApiClientProvider.class),
                mock(PasswordEncoder.class),
                mock(UserDataCleanupService.class)
        );

        ReflectionTestUtils.setField(service, "frontendBaseUrl", "https://capstone.studylink.click");
        ReflectionTestUtils.setField(service, "allowedFrontendOrigins", allowedOrigins);
        return service;
    }

    @Test
    void desktopFrontendOriginIsKeptWhenAllowed() {
        GoogleOAuthService service = createServiceWithAllowedOrigins(
                List.of("https://capstone.studylink.click", " maily://app ")
        );

        String resolved = ReflectionTestUtils.invokeMethod(
                service,
                "resolveFrontendOrigin",
                "maily://app"
        );

        assertThat(resolved).isEqualTo("maily://app");
    }

    @Test
    void unlistedDesktopFrontendOriginIsRejectedInsteadOfFallingBackToWebFrontend() {
        GoogleOAuthService service = createServiceWithAllowedOrigins(
                List.of("https://capstone.studylink.click")
        );

        assertThatThrownBy(() -> ReflectionTestUtils.invokeMethod(
                        service,
                        "resolveFrontendOrigin",
                        "maily://app"
                ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("허용되지 않은 데스크톱 OAuth origin입니다.");
    }
}
