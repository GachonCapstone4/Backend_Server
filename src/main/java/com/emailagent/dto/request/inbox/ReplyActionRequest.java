package com.emailagent.dto.request.inbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class ReplyActionRequest {

    @NotBlank
    private String action; // SEND | EDIT_SEND | SAVE_DRAFT | SKIP

    private String subject; // EDIT_SEND일 때 선택적으로 사용 (수정된 제목)

    private String content; // EDIT_SEND일 때만 사용 (수정된 본문)

    @JsonProperty("recommendation_id")
    private Long recommendationId; // SAVE_DRAFT에서 선택한 추천 템플릿 연결용

    @JsonProperty("manual_draft")
    private Boolean manualDraft; // SAVE_DRAFT에서 직접 작성 초안 여부
}
