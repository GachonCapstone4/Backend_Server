package com.emailagent.dto.response.admin.support;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminSupportTicketListResponse extends BaseResponse {

    @JsonProperty("total_count")
    private final long totalCount;

    private final List<TicketItem> tickets;

    public AdminSupportTicketListResponse(long totalCount, List<TicketItem> tickets) {
        this.totalCount = totalCount;
        this.tickets = tickets;
    }

    @Getter
    public static class TicketItem {

        @JsonProperty("ticket_id")
        private final long ticketId;

        @JsonProperty("user_id")
        private final long userId;

        private final String title;

        private final String status;

        @JsonProperty("created_at")
        private final String createdAt;

        public TicketItem(long ticketId, long userId, String title, String status, String createdAt) {
            this.ticketId = ticketId;
            this.userId = userId;
            this.title = title;
            this.status = status;
            this.createdAt = createdAt;
        }
    }
}
