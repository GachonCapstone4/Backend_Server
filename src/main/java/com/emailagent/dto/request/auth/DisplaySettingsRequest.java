package com.emailagent.dto.request.auth;

import com.emailagent.domain.value.UserDisplayConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class DisplaySettingsRequest {

    private String theme;

    private List<Widget> widgets;

    public UserDisplayConfig toConfig() {
        return UserDisplayConfig.builder()
                .theme(theme)
                .widgets(widgets == null ? List.of() : widgets.stream()
                        .filter(Objects::nonNull)
                        .map(widget -> UserDisplayConfig.Widget.builder()
                                .id(widget.id)
                                .visible(widget.visible)
                                .build())
                        .toList())
                .build();
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Widget {
        private String id;

        @JsonProperty("visible")
        private Boolean visible;
    }
}
