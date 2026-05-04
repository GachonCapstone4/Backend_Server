package com.emailagent.dto.response.business;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TemplateRegenerateResponse extends BaseResponse {

    private String status;

    @JsonProperty("processing_count")
    private int processingCount;

    @JsonProperty("job_ids")
    private List<String> jobIds;

    public static TemplateRegenerateResponse of(int processingCount, List<String> jobIds) {
        return TemplateRegenerateResponse.builder()
                .status("PROCESSING")
                .processingCount(processingCount)
                .jobIds(jobIds)
                .build();
    }
}
