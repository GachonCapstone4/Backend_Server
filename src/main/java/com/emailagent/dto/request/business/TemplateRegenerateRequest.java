package com.emailagent.dto.request.business;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import java.util.List;

@Getter
public class TemplateRegenerateRequest {

    @JsonProperty("regenerate_all")
    private boolean regenerateAll;

    @JsonProperty("template_ids")
    private List<Long> templateIds; // regenerateAll=false 일 때만 사용
}
