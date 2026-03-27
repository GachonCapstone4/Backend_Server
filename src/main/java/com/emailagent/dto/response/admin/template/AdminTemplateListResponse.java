package com.emailagent.dto.response.admin.template;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminTemplateListResponse extends BaseResponse {

    @JsonProperty("total_count")
    private final long totalCount;

    private final List<TemplateItem> templates;

    public AdminTemplateListResponse(long totalCount, List<TemplateItem> templates) {
        this.totalCount = totalCount;
        this.templates = templates;
    }

    @Getter
    public static class TemplateItem {

        @JsonProperty("template_id")
        private final long templateId;

        @JsonProperty("user_id")
        private final long userId;

        private final String title;

        @JsonProperty("created_at")
        private final String createdAt;

        public TemplateItem(long templateId, long userId, String title, String createdAt) {
            this.templateId = templateId;
            this.userId = userId;
            this.title = title;
            this.createdAt = createdAt;
        }
    }
}
