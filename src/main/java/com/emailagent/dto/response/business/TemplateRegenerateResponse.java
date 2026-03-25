package com.emailagent.dto.response.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TemplateRegenerateResponse {

    private boolean success;
    private String message;
    private String status;
    private ProcessingData data;

    @Getter
    @Builder
    public static class ProcessingData {
        @JsonProperty("processing_count")
        private int processingCount;
    }

    public static TemplateRegenerateResponse of(int processingCount) {
        return TemplateRegenerateResponse.builder()
                .success(true)
                .message(processingCount + "개의 템플릿 재생성이 요청되었습니다.")
                .status("PROCESSING")
                .data(ProcessingData.builder()
                        .processingCount(processingCount)
                        .build())
                .build();
    }
}
