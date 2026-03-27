package com.emailagent.dto.response.admin.dashboard;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminDashboardSummaryResponse extends BaseResponse {

    @JsonProperty("total_users")
    private final long totalUsers;

    @JsonProperty("connected_users")
    private final long connectedUsers;

    @JsonProperty("today_analyzed_emails")
    private final long todayAnalyzedEmails;

    @JsonProperty("today_generated_drafts")
    private final long todayGeneratedDrafts;

    @JsonProperty("total_support_tickets")
    private final long totalSupportTickets;

    public AdminDashboardSummaryResponse(long totalUsers, long connectedUsers,
                                         long todayAnalyzedEmails, long todayGeneratedDrafts,
                                         long totalSupportTickets) {
        this.totalUsers = totalUsers;
        this.connectedUsers = connectedUsers;
        this.todayAnalyzedEmails = todayAnalyzedEmails;
        this.todayGeneratedDrafts = todayGeneratedDrafts;
        this.totalSupportTickets = totalSupportTickets;
    }
}
