package com.emailagent.dto.response.admin.support;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminSupportTicketDetailResponse extends BaseResponse {

    @JsonProperty("ticket_id")
    private final long ticketId;

    @JsonProperty("user_id")
    private final long userId;

    private final String title;

    private final String content;

    private final String status;

    @JsonProperty("admin_reply")
    private final String adminReply;

    @JsonProperty("replied_by")
    private final Long repliedBy;

    @JsonProperty("replied_at")
    private final String repliedAt;

    @JsonProperty("created_at")
    private final String createdAt;

    public AdminSupportTicketDetailResponse(long ticketId, long userId, String title,
                                             String content, String status, String adminReply,
                                             Long repliedBy, String repliedAt, String createdAt) {
        this.ticketId = ticketId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.adminReply = adminReply;
        this.repliedBy = repliedBy;
        this.repliedAt = repliedAt;
        this.createdAt = createdAt;
    }
}
