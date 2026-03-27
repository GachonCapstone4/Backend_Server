package com.emailagent.dto.response.admin.user;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminUserIntegrationResponse extends BaseResponse {

    @JsonProperty("is_gmail_connected")
    private final Boolean gmailConnected;

    @JsonProperty("is_calendar_connected")
    private final boolean calendarConnected;

    @JsonProperty("integrated_email")
    private final String integratedEmail;

    @JsonProperty("integrated_at")
    private final String integratedAt;

    @JsonProperty("last_sync_at")
    private final String lastSyncAt;

    public AdminUserIntegrationResponse(Boolean gmailConnected, boolean calendarConnected,
                                         String integratedEmail, String integratedAt,
                                         String lastSyncAt) {
        this.gmailConnected = gmailConnected;
        this.calendarConnected = calendarConnected;
        this.integratedEmail = integratedEmail;
        this.integratedAt = integratedAt;
        this.lastSyncAt = lastSyncAt;
    }
}
