package com.emailagent.dto.response.admin.operation;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminJobDetailResponse extends BaseResponse {

    @JsonProperty("outbox_id")
    private final long outboxId;

    @JsonProperty("email_id")
    private final long emailId;

    private final String status;

    private final String payload;

    @JsonProperty("retry_count")
    private final int retryCount;

    @JsonProperty("max_retry")
    private final int maxRetry;

    @JsonProperty("created_at")
    private final String createdAt;

    @JsonProperty("sent_at")
    private final String sentAt;

    @JsonProperty("finished_at")
    private final String finishedAt;

    public AdminJobDetailResponse(long outboxId, long emailId, String status, String payload,
                                   int retryCount, int maxRetry, String createdAt,
                                   String sentAt, String finishedAt) {
        this.outboxId = outboxId;
        this.emailId = emailId;
        this.status = status;
        this.payload = payload;
        this.retryCount = retryCount;
        this.maxRetry = maxRetry;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
        this.finishedAt = finishedAt;
    }
}
