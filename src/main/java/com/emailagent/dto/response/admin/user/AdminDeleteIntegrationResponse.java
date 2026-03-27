package com.emailagent.dto.response.admin.user;

import com.emailagent.dto.response.auth.BaseResponse;
import lombok.Getter;

@Getter
public class AdminDeleteIntegrationResponse extends BaseResponse {

    private final boolean success;

    public AdminDeleteIntegrationResponse(boolean success) {
        this.success = success;
    }
}
