package com.emailagent.domain.converter;

import com.emailagent.domain.enums.NotificationType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User.notification_configs (JSON) ↔ Map<String, Boolean> 변환 컨버터.
 * DB 값이 null이거나 파싱 실패 시 모든 타입이 true인 기본값을 반환한다.
 */
@Converter
public class NotificationConfigConverter implements AttributeConverter<Map<String, Boolean>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** 모든 알림 타입이 활성화된 기본 설정 (신규 유저 / null 복구용) */
    public static Map<String, Boolean> defaultConfigs() {
        Map<String, Boolean> configs = new LinkedHashMap<>();
        for (NotificationType type : NotificationType.values()) {
            configs.put(type.name(), true);
        }
        return configs;
    }

    @Override
    public String convertToDatabaseColumn(Map<String, Boolean> attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("notification_configs JSON 직렬화 실패", e);
        }
    }

    @Override
    public Map<String, Boolean> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return defaultConfigs();
        try {
            return MAPPER.readValue(dbData, new TypeReference<>() {});
        } catch (Exception e) {
            return defaultConfigs();
        }
    }
}
