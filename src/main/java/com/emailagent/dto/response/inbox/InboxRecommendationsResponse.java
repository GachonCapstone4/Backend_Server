package com.emailagent.dto.response.inbox;

import com.emailagent.domain.entity.EmailTemplateRecommendation;
import com.emailagent.domain.entity.Template;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class InboxRecommendationsResponse {

    @JsonProperty("drafts")
    private List<RecommendationItem> drafts;

    @Getter
    @Builder
    public static class RecommendationItem {
        @JsonProperty("draft_id")
        private Long draftId;

        @JsonProperty("template_title")
        private String templateTitle;

        @JsonProperty("template_id")
        private Long templateId;

        @JsonProperty("subject")
        private String subject;

        @JsonProperty("body")
        private String body;

        @JsonProperty("similarity")
        private Double similarity;

        @JsonProperty("email_id")
        private Long emailId;

        @JsonProperty("auto_completed_count")
        private Integer autoCompletedCount;

        @JsonProperty("auto_completed_keys")
        private List<String> autoCompletedKeys;

        @JsonProperty("auto_completed_values")
        private java.util.Map<String, String> autoCompletedValues;

        @JsonProperty("required_input_count")
        private Integer requiredInputCount;

        @JsonProperty("required_input_keys")
        private List<String> requiredInputKeys;

        public static RecommendationItem from(
                EmailTemplateRecommendation recommendation,
                String subject,
                String body,
                Integer autoCompletedCount,
                List<String> autoCompletedKeys,
                java.util.Map<String, String> autoCompletedValues,
                Integer requiredInputCount,
                List<String> requiredInputKeys
        ) {
            Template template = recommendation.getTemplate();
            return RecommendationItem.builder()
                    .draftId(recommendation.getRecommendationId())
                    .templateTitle(template.getTitle())
                    .templateId(template.getTemplateId())
                    .subject(subject)
                    .body(body)
                    .similarity(recommendation.getScore())
                    .emailId(recommendation.getEmail().getEmailId())
                    .autoCompletedCount(autoCompletedCount)
                    .autoCompletedKeys(autoCompletedKeys)
                    .autoCompletedValues(autoCompletedValues)
                    .requiredInputCount(requiredInputCount)
                    .requiredInputKeys(requiredInputKeys)
                    .build();
        }
    }
}
