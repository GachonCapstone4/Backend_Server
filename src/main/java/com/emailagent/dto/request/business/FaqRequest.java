package com.emailagent.dto.request.business;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FaqRequest {

    @NotBlank
    private String question;

    @NotBlank
    private String answer;
}
