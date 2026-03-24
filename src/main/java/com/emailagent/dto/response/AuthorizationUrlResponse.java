package com.emailagent.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AuthorizationUrlResponse extends BaseResponse {

    @JsonProperty("authorization_url")
    private final String authorizationUrl;

    public AuthorizationUrlResponse(String authorizationUrl) {
        this.authorizationUrl = authorizationUrl;
    }
}
