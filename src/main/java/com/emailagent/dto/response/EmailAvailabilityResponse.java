package com.emailagent.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class EmailAvailabilityResponse extends BaseResponse {

    @JsonProperty("is_available")
    private final boolean isAvailable;

    public EmailAvailabilityResponse(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}
