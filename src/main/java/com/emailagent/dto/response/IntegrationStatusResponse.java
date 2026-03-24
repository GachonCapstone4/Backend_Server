package com.emailagent.dto.response;

import com.emailagent.domain.entity.Integration;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.ZoneOffset;

@Getter
public class IntegrationStatusResponse extends BaseResponse {

    @JsonProperty("sync_status")
    private final String syncStatus;

    @JsonProperty("updated_at")
    private final String updatedAt;

    public IntegrationStatusResponse(Integration integration) {
        this.syncStatus = integration.getSyncStatus().name();
        this.updatedAt = integration.getUpdatedAt().toInstant(ZoneOffset.UTC).toString();
    }
}
