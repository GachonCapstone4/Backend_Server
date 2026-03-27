package com.emailagent.dto.response.admin.user;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminUserStatusUpdateResponse extends BaseResponse {

    @JsonProperty("user_id")
    private final long userId;

    @JsonProperty("is_active")
    private final boolean active;

    public AdminUserStatusUpdateResponse(long userId, boolean active) {
        this.userId = userId;
        this.active = active;
    }
}
