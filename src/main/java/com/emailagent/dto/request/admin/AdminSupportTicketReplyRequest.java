package com.emailagent.dto.request.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AdminSupportTicketReplyRequest {

    @NotBlank
    @JsonProperty("admin_reply")
    private String adminReply;
}
