package com.emailagent.dto.response;

import com.emailagent.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.ZoneOffset;

@Getter
public class SignupResponse extends BaseResponse {

    @JsonProperty("user_id")
    private final Long userId;

    private final String email;

    @JsonProperty("created_at")
    private final String createdAt;

    public SignupResponse(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.createdAt = user.getCreatedAt().toInstant(ZoneOffset.UTC).toString();
    }
}
