package com.emailagent.dto.response.notification;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * GET/PATCH /api/notifications/settings 응답.
 * @JsonAnyGetter 로 Map entries 를 BaseResponse 와 같은 레벨(Flat)로 직렬화한다.
 */
public class NotificationSettingsResponse extends BaseResponse {

    private static final String DRAFT_THRESHOLD_KEY = "DRAFT_THRESHOLD";

    private final Map<String, Object> settings;

    public NotificationSettingsResponse(Map<String, Boolean> settings, Integer draftThreshold) {
        super();
        Map<String, Object> responseSettings = new LinkedHashMap<>(settings);
        responseSettings.put(DRAFT_THRESHOLD_KEY, draftThreshold != null ? draftThreshold : 3);
        this.settings = responseSettings;
    }

    @JsonAnyGetter
    public Map<String, Object> getSettings() {
        return settings;
    }
}
