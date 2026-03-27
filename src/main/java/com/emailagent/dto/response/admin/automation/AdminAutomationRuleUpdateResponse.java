package com.emailagent.dto.response.admin.automation;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class AdminAutomationRuleUpdateResponse extends BaseResponse {

    @JsonProperty("rule_id")
    private final long ruleId;

    public AdminAutomationRuleUpdateResponse(long ruleId) {
        this.ruleId = ruleId;
    }
}
