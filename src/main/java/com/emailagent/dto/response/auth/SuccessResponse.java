package com.emailagent.dto.response.auth;

import lombok.Getter;

@Getter
public class SuccessResponse extends BaseResponse {

    private final boolean success;

    public SuccessResponse(boolean success) {
        this.success = success;
    }
}
