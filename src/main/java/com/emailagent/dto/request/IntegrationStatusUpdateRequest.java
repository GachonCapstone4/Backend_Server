package com.emailagent.dto.request;

import com.emailagent.domain.enums.SyncStatus;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class IntegrationStatusUpdateRequest {

    @NotNull(message = "sync_status는 필수입니다.")
    @JsonProperty("sync_status")
    private SyncStatus syncStatus;
}
