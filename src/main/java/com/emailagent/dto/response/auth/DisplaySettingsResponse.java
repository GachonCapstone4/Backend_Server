package com.emailagent.dto.response.auth;

import com.emailagent.domain.value.UserDisplayConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class DisplaySettingsResponse extends BaseResponse {

    private final String theme;

    private final List<Widget> widgets;

    public DisplaySettingsResponse(UserDisplayConfig config) {
        super();
        this.theme = config.getTheme();
        this.widgets = config.getWidgets().stream()
                .map(Widget::new)
                .toList();
    }

    @Getter
    public static class Widget {
        private final String id;

        @JsonProperty("visible")
        private final boolean visible;

        public Widget(UserDisplayConfig.Widget widget) {
            this.id = widget.getId();
            this.visible = Boolean.TRUE.equals(widget.getVisible());
        }
    }
}
