package com.emailagent.dto.request;

import com.emailagent.domain.entity.BusinessProfile.EmailTone;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class BusinessProfileRequest {

    private String industryType;

    @NotNull
    private EmailTone emailTone;

    private String companyDescription;
}
