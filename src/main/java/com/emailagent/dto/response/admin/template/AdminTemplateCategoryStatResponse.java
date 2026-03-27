package com.emailagent.dto.response.admin.template;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminTemplateCategoryStatResponse extends BaseResponse {

    private final List<CategoryStat> statistics;

    public AdminTemplateCategoryStatResponse(List<CategoryStat> statistics) {
        this.statistics = statistics;
    }

    @Getter
    public static class CategoryStat {

        @JsonProperty("category_id")
        private final long categoryId;

        @JsonProperty("category_name")
        private final String categoryName;

        @JsonProperty("template_count")
        private final long templateCount;

        @JsonProperty("usage_count")
        private final long usageCount;

        public CategoryStat(long categoryId, String categoryName, long templateCount, long usageCount) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.templateCount = templateCount;
            this.usageCount = usageCount;
        }
    }
}
