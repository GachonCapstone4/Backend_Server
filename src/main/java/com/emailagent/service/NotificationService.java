package com.emailagent.service;

import com.emailagent.domain.converter.NotificationConfigConverter;
import com.emailagent.domain.entity.Notification;
import com.emailagent.domain.entity.User;
import com.emailagent.domain.enums.NotificationType;
import com.emailagent.dto.response.auth.BaseResponse;
import com.emailagent.dto.response.notification.NotificationResponse;
import com.emailagent.dto.response.notification.NotificationSettingsResponse;
import com.emailagent.exception.ResourceNotFoundException;
import com.emailagent.repository.NotificationRepository;
import com.emailagent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // =============================================
    // GET /api/notifications?is_read=false
    // =============================================
    @Transactional(readOnly = true)
    public NotificationResponse.ListResponse getNotifications(Long userId, Boolean isRead) {
        List<NotificationResponse> data;

        if (Boolean.FALSE.equals(isRead)) {
            data = notificationRepository
                    .findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(NotificationResponse::from)
                    .toList();
        } else {
            data = notificationRepository
                    .findByUser_UserIdOrderByCreatedAtDesc(userId)
                    .stream()
                    .map(NotificationResponse::from)
                    .toList();
        }

        return NotificationResponse.ListResponse.builder()
                .notifications(data)
                .build();
    }

    // =============================================
    // PATCH /api/notifications/{notification_id}/read
    // =============================================
    @Transactional
    public BaseResponse markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository
                .findByNotificationIdAndUser_UserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("알림을 찾을 수 없습니다."));

        notification.markAsRead();

        return new BaseResponse();
    }

    // =============================================
    // PATCH /api/notifications/read-all
    // =============================================
    @Transactional
    public BaseResponse markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
        return new BaseResponse();
    }

    // =============================================
    // GET /api/notifications/settings
    // =============================================
    @Transactional(readOnly = true)
    public NotificationSettingsResponse getSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));
        Map<String, Boolean> configs = user.getNotificationConfigs();
        return new NotificationSettingsResponse(
                configs != null ? configs : NotificationConfigConverter.defaultConfigs()
        );
    }

    // =============================================
    // PATCH /api/notifications/settings
    // 전달된 키만 업데이트 (나머지 유지)
    // =============================================
    @Transactional
    public NotificationSettingsResponse updateSettings(Long userId, Map<String, Boolean> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        Map<String, Boolean> current = user.getNotificationConfigs();
        if (current == null) {
            current = NotificationConfigConverter.defaultConfigs();
        }

        // 유효한 NotificationType 키만 반영, 알 수 없는 키는 무시
        for (NotificationType type : NotificationType.values()) {
            Boolean value = updates.get(type.name());
            if (value != null) {
                current.put(type.name(), value);
            }
        }

        user.updateNotificationConfigs(current);
        return new NotificationSettingsResponse(current);
    }

    // =============================================
    // 알림 생성 (비즈니스 이벤트 트리거용)
    // 유저 설정에서 해당 type이 true인 경우에만 INSERT
    // 호출자 트랜잭션에 참여 (REQUIRED)
    // =============================================
    @Transactional
    public void createNotification(User user, NotificationType type, String title, String notiMessage, Long relatedId) {
        Map<String, Boolean> configs = user.getNotificationConfigs();
        // configs null = 기존 유저 마이그레이션 전 상태 → 기본값(전체 허용)으로 처리
        if (configs != null && Boolean.FALSE.equals(configs.get(type.name()))) {
            return;
        }
        notificationRepository.save(Notification.builder()
                .user(user)
                .type(type.name())
                .title(title)
                .notiMessage(notiMessage)
                .relatedId(relatedId)
                .build());
    }
}
