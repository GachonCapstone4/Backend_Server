package com.emailagent.dto.response.admin.user;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminUserListResponse extends BaseResponse {

    @JsonProperty("total_count")
    private final long totalCount;

    private final List<UserItem> users;

    public AdminUserListResponse(long totalCount, List<UserItem> users) {
        this.totalCount = totalCount;
        this.users = users;
    }

    @Getter
    public static class UserItem {

        @JsonProperty("user_id")
        private final long userId;

        private final String email;

        private final String name;

        @JsonProperty("industry_type")
        private final String industryType;

        @JsonProperty("is_active")
        private final boolean active;

        @JsonProperty("created_at")
        private final String createdAt;

        public UserItem(long userId, String email, String name,
                        String industryType, boolean active, String createdAt) {
            this.userId = userId;
            this.email = email;
            this.name = name;
            this.industryType = industryType;
            this.active = active;
            this.createdAt = createdAt;
        }
    }
}
