package com.emailagent.dto.response.auth;

import com.emailagent.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.ZoneOffset;

@Getter
public class UserUpdateResponse extends BaseResponse {

    @JsonProperty("user_id")
    private final Long userId;

    private final String name;

    @JsonProperty("updated_at")
    private final String updatedAt;

    public UserUpdateResponse(User user) {
        this.userId = user.getUserId();
        this.name = user.getName();
        this.updatedAt = user.getUpdatedAt().toInstant(ZoneOffset.UTC).toString();
    }
}
