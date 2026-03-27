package com.emailagent.dto.response.admin.user;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminUserDetailResponse extends BaseResponse {

    @JsonProperty("user_id")
    private final long userId;

    private final String email;

    private final String name;

    private final String role;

    @JsonProperty("is_active")
    private final boolean active;

    @JsonProperty("last_login_at")
    private final String lastLoginAt;

    @JsonProperty("industry_type")
    private final String industryType;

    @JsonProperty("email_tone")
    private final String emailTone;

    @JsonProperty("company_desc")
    private final String companyDesc;

    @JsonProperty("total_processed_emails")
    private final long totalProcessedEmails;

    @JsonProperty("total_generated_drafts")
    private final long totalGeneratedDrafts;

    @JsonProperty("recent_ticket_count")
    private final long recentTicketCount;

    public AdminUserDetailResponse(long userId, String email, String name, String role,
                                    boolean active, String lastLoginAt, String industryType,
                                    String emailTone, String companyDesc,
                                    long totalProcessedEmails, long totalGeneratedDrafts,
                                    long recentTicketCount) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
        this.active = active;
        this.lastLoginAt = lastLoginAt;
        this.industryType = industryType;
        this.emailTone = emailTone;
        this.companyDesc = companyDesc;
        this.totalProcessedEmails = totalProcessedEmails;
        this.totalGeneratedDrafts = totalGeneratedDrafts;
        this.recentTicketCount = recentTicketCount;
    }
}
