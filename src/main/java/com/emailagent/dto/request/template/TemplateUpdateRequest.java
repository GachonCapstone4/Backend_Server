package com.emailagent.dto.request.template;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class TemplateUpdateRequest {
    @NotBlank private String title;
    @NotBlank private String subjectTemplate;
    @NotBlank private String bodyTemplate;
}
