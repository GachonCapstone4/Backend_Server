package com.emailagent.dto.response.admin.operation;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminJobListResponse extends BaseResponse {

    @JsonProperty("total_count")
    private final long totalCount;

    private final List<JobItem> jobs;

    public AdminJobListResponse(long totalCount, List<JobItem> jobs) {
        this.totalCount = totalCount;
        this.jobs = jobs;
    }

    @Getter
    public static class JobItem {

        @JsonProperty("outbox_id")
        private final long outboxId;

        @JsonProperty("email_id")
        private final long emailId;

        private final String status;

        @JsonProperty("created_at")
        private final String createdAt;

        public JobItem(long outboxId, long emailId, String status, String createdAt) {
            this.outboxId = outboxId;
            this.emailId = emailId;
            this.status = status;
            this.createdAt = createdAt;
        }
    }
}
