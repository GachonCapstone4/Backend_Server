package com.emailagent.service;

import com.emailagent.domain.entity.Integration;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.gmail.Gmail;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.UserCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Google API 클라이언트 인스턴스 생성 및 자격증명 관리를 담당하는 컴포넌트.
 * Integration 엔티티에 저장된 토큰을 기반으로 Gmail, Calendar 클라이언트를 빌드한다.
 */
@Slf4j
@Component
public class GoogleApiClientProvider {

    private static final String APPLICATION_NAME = "EmailAgent";

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    /**
     * 저장된 토큰으로 Gmail 서비스 클라이언트 생성
     */
    public Gmail buildGmailClient(Integration integration) throws GeneralSecurityException, IOException {
        HttpCredentialsAdapter credentials = buildCredentialsAdapter(integration);
        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credentials
        ).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * 저장된 토큰으로 Calendar 서비스 클라이언트 생성
     */
    public Calendar buildCalendarClient(Integration integration) throws GeneralSecurityException, IOException {
        HttpCredentialsAdapter credentials = buildCredentialsAdapter(integration);
        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credentials
        ).setApplicationName(APPLICATION_NAME).build();
    }

    /**
     * Integration 엔티티의 토큰으로 UserCredentials 생성.
     * Access Token이 만료되면 Refresh Token으로 자동 갱신된다.
     */
    private HttpCredentialsAdapter buildCredentialsAdapter(Integration integration) {
        Date expiryDate = integration.getTokenExpiresAt() != null
                ? Date.from(integration.getTokenExpiresAt().toInstant(ZoneOffset.UTC))
                : null;

        AccessToken accessToken = new AccessToken(integration.getAccessToken(), expiryDate);

        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(integration.getRefreshToken())
                .setAccessToken(accessToken)
                .build();

        return new HttpCredentialsAdapter(credentials);
    }
}
