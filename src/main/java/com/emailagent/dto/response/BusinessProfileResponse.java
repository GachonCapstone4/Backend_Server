package com.emailagent.dto.response;

import com.emailagent.domain.entity.BusinessProfile;
import com.emailagent.domain.entity.BusinessProfile.EmailTone;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BusinessProfileResponse {
    private Long profileId;
    private String industryType;
    private EmailTone emailTone;
    private String companyDescription;

    public static BusinessProfileResponse from(BusinessProfile profile) {
        return BusinessProfileResponse.builder()
                .profileId(profile.getProfileId())
                .industryType(profile.getIndustryType())
                .emailTone(profile.getEmailTone())
                .companyDescription(profile.getCompanyDescription())
                .build();
    }
}
