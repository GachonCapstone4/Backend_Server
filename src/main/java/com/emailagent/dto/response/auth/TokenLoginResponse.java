package com.emailagent.dto.response.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TokenLoginResponse extends BaseResponse {

    @JsonProperty("access_token")
    private final String accessToken;

    @JsonProperty("token_type")
    private final String tokenType = "Bearer";

    @JsonProperty("expires_in")
    private final long expiresIn; // 초 단위

    public TokenLoginResponse(String accessToken, long expiresInMs) {
        this.accessToken = accessToken;
        this.expiresIn = expiresInMs / 1000;
    }
}
