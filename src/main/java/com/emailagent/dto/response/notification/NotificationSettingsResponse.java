package com.emailagent.dto.response.notification;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.Map;

/**
 * GET/PATCH /api/notifications/settings 응답.
 * @JsonAnyGetter 로 Map entries 를 BaseResponse 와 같은 레벨(Flat)로 직렬화한다.
 */
public class NotificationSettingsResponse extends BaseResponse {

    private final Map<String, Boolean> settings;

    public NotificationSettingsResponse(Map<String, Boolean> settings) {
        super();
        this.settings = settings;
    }

    @JsonAnyGetter
    public Map<String, Boolean> getSettings() {
        return settings;
    }
}
