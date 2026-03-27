package com.emailagent.dto.response.admin.support;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminSupportTicketReplyResponse extends BaseResponse {

    @JsonProperty("ticket_id")
    private final long ticketId;

    private final String status;

    public AdminSupportTicketReplyResponse(long ticketId, String status) {
        this.ticketId = ticketId;
        this.status = status;
    }
}
