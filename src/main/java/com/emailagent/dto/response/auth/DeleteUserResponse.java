package com.emailagent.dto.response.auth;

import lombok.Getter;

@Getter
public class DeleteUserResponse extends BaseResponse {

    private final boolean success;

    public DeleteUserResponse(boolean success) {
        this.success = success;
    }
}
