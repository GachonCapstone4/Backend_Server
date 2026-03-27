package com.emailagent.dto.response.admin.operation;

import com.emailagent.dto.response.auth.BaseResponse;
import lombok.Getter;

@Getter
public class AdminJobDeleteResponse extends BaseResponse {

    private final boolean success;

    public AdminJobDeleteResponse(boolean success) {
        this.success = success;
    }
}
