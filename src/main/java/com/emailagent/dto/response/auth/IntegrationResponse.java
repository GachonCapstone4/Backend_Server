package com.emailagent.dto.response.auth;

import com.emailagent.domain.entity.Integration;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.ZoneOffset;

@Getter
public class IntegrationResponse extends BaseResponse {

    private final String provider;

    @JsonProperty("connected_email")
    private final String connectedEmail;

    @JsonProperty("sync_status")
    private final String syncStatus;

    @JsonProperty("last_synced_at")
    private final String lastSyncedAt;

    public IntegrationResponse(Integration integration) {
        this.provider = integration.getProvider();
        this.connectedEmail = integration.getConnectedEmail();
        this.syncStatus = integration.getSyncStatus().name();
        this.lastSyncedAt = integration.getLastSyncedAt() != null
                ? integration.getLastSyncedAt().toInstant(ZoneOffset.UTC).toString()
                : null;
    }
}
