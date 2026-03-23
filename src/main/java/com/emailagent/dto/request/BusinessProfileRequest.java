package com.emailagent.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class BusinessProfileRequest {

    @NotBlank
    private String industryType;

    private String companyDescription;

    @NotBlank
    private String emailTone; // FORMAL / NEUTRAL / FRIENDLY
}