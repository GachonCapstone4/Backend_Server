package com.emailagent.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AuthMeResponse extends BaseResponse {

    @JsonProperty("is_authenticated")
    private final boolean isAuthenticated = true;

    @JsonProperty("user_id")
    private final Long userId;

    public AuthMeResponse(Long userId) {
        this.userId = userId;
    }
}
