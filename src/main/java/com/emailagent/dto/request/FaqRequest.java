package com.emailagent.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class FaqRequest {
    @NotBlank
    private String question;

    @NotBlank
    private String answer;
}
