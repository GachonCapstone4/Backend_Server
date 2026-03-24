package com.emailagent.dto.response;

import com.emailagent.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.ZoneOffset;

@Getter
public class UserProfileResponse extends BaseResponse {

    @JsonProperty("user_id")
    private final Long userId;

    private final String email;
    private final String name;
    private final String role;

    @JsonProperty("created_at")
    private final String createdAt;

    public UserProfileResponse(User user) {
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.role = user.getRole().name();
        this.createdAt = user.getCreatedAt().toInstant(ZoneOffset.UTC).toString();
    }
}
