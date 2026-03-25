package com.emailagent.dto.request.business;

import com.emailagent.domain.enums.EmailTone;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BusinessProfileRequest {

    @NotBlank
    @JsonProperty("industry_type")
    private String industryType;

    @JsonProperty("company_description")
    private String companyDescription;

    @NotNull
    @JsonProperty("email_tone")
    private EmailTone emailTone; // FORMAL / NEUTRAL / FRIENDLY
}
