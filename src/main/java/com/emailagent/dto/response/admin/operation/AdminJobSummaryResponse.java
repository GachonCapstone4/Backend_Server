package com.emailagent.dto.response.admin.operation;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminJobSummaryResponse extends BaseResponse {

    @JsonProperty("ready_count")
    private final long readyCount;

    @JsonProperty("success_count")
    private final long successCount;

    @JsonProperty("failed_count")
    private final long failedCount;

    public AdminJobSummaryResponse(long readyCount, long successCount, long failedCount) {
        this.readyCount = readyCount;
        this.successCount = successCount;
        this.failedCount = failedCount;
    }
}
