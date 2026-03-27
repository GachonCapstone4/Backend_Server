package com.emailagent.dto.response.admin.operation;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminJobErrorResponse extends BaseResponse {

    @JsonProperty("outbox_id")
    private final long outboxId;

    @JsonProperty("fail_reason")
    private final String failReason;

    public AdminJobErrorResponse(long outboxId, String failReason) {
        this.outboxId = outboxId;
        this.failReason = failReason;
    }
}
