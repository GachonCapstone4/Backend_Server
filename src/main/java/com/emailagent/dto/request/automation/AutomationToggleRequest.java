package com.emailagent.dto.request.automation;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AutomationToggleRequest {

    // auto-send 토글 시 사용
    @JsonProperty("auto_send_enabled")
    private Boolean autoSendEnabled;

    // auto-calendar 토글 시 사용
    @JsonProperty("auto_calendar_enabled")
    private Boolean autoCalendarEnabled;
}
