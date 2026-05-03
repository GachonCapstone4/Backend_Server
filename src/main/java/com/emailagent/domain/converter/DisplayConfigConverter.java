package com.emailagent.domain.converter;

import com.emailagent.domain.value.UserDisplayConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * User.display_configs (JSON) ↔ 사용자 화면 설정 변환 컨버터.
 * DB 값이 없거나 깨져 있으면 기본 대시보드 위젯 구성을 반환한다.
 */
@Converter
public class DisplayConfigConverter implements AttributeConverter<UserDisplayConfig, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final List<String> DEFAULT_WIDGET_ORDER = List.of("upcoming", "emails", "summary");

    public static UserDisplayConfig defaultConfig() {
        return UserDisplayConfig.builder()
                .theme("light")
                .widgets(List.of(
                        UserDisplayConfig.Widget.builder().id("upcoming").visible(true).build(),
                        UserDisplayConfig.Widget.builder().id("emails").visible(true).build(),
                        UserDisplayConfig.Widget.builder().id("summary").visible(true).build()
                ))
                .build();
    }

    public static UserDisplayConfig normalize(UserDisplayConfig config) {
        UserDisplayConfig fallback = defaultConfig();
        if (config == null) {
            return fallback;
        }

        String theme = "dark".equals(config.getTheme()) ? "dark" : "light";
        Map<String, Boolean> visibleById = new LinkedHashMap<>();
        if (config.getWidgets() != null) {
            for (UserDisplayConfig.Widget widget : config.getWidgets()) {
                if (widget == null || widget.getId() == null || !DEFAULT_WIDGET_ORDER.contains(widget.getId())) {
                    continue;
                }
                visibleById.put(widget.getId(), Boolean.TRUE.equals(widget.getVisible()));
            }
        }

        List<UserDisplayConfig.Widget> normalizedWidgets = new ArrayList<>();
        for (String widgetId : DEFAULT_WIDGET_ORDER) {
            boolean defaultVisible = fallback.getWidgets().stream()
                    .filter(widget -> widgetId.equals(widget.getId()))
                    .findFirst()
                    .map(UserDisplayConfig.Widget::getVisible)
                    .orElse(false);
            normalizedWidgets.add(UserDisplayConfig.Widget.builder()
                    .id(widgetId)
                    .visible(visibleById.getOrDefault(widgetId, defaultVisible))
                    .build());
        }

        return UserDisplayConfig.builder()
                .theme(theme)
                .widgets(normalizedWidgets)
                .build();
    }

    @Override
    public String convertToDatabaseColumn(UserDisplayConfig attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(normalize(attribute));
        } catch (Exception e) {
            throw new IllegalStateException("display_configs JSON 직렬화 실패", e);
        }
    }

    @Override
    public UserDisplayConfig convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return defaultConfig();
        try {
            return normalize(MAPPER.readValue(dbData, UserDisplayConfig.class));
        } catch (Exception e) {
            return defaultConfig();
        }
    }
}
