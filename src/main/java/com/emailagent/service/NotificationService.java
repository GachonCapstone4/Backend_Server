package com.emailagent.service;

import com.emailagent.domain.converter.NotificationConfigConverter;
import com.emailagent.domain.entity.Notification;
import com.emailagent.domain.entity.User;
import com.emailagent.domain.enums.DraftStatus;
import com.emailagent.domain.enums.NotificationType;
import com.emailagent.dto.response.auth.BaseResponse;
import com.emailagent.dto.response.notification.NotificationResponse;
import com.emailagent.dto.response.notification.NotificationSettingsResponse;
import com.emailagent.exception.ResourceNotFoundException;
import com.emailagent.rabbitmq.event.SseEvent;
import com.emailagent.repository.DraftReplyRepository;
import com.emailagent.repository.EmailTemplateRecommendationRepository;
import com.emailagent.repository.NotificationRepository;
import com.emailagent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int DEFAULT_DRAFT_THRESHOLD = 3;
    private static final int MIN_DRAFT_THRESHOLD = 1;
    private static final int MAX_DRAFT_THRESHOLD = 99;
    private static final String DRAFT_THRESHOLD_KEY = "DRAFT_THRESHOLD";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final DraftReplyRepository draftReplyRepository;
    private final EmailTemplateRecommendationRepository recommendationRepository;
    private final ApplicationEventPublisher eventPublisher;

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
                configs != null ? configs : NotificationConfigConverter.defaultConfigs(),
                normalizeDraftThreshold(user.getNotificationDraftThreshold())
        );
    }

    // =============================================
    // PATCH /api/notifications/settings
    // 전달된 키만 업데이트 (나머지 유지)
    // =============================================
    @Transactional
    public NotificationSettingsResponse updateSettings(Long userId, Map<String, Object> updates) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        Map<String, Boolean> current = user.getNotificationConfigs();
        if (current == null) {
            current = NotificationConfigConverter.defaultConfigs();
        }

        // 유효한 NotificationType 키만 반영, 알 수 없는 키는 무시
        for (NotificationType type : NotificationType.values()) {
            Boolean value = toBoolean(updates.get(type.name()));
            if (value != null) {
                current.put(type.name(), value);
            }
        }

        Integer draftThreshold = toInteger(updates.get(DRAFT_THRESHOLD_KEY));
        if (draftThreshold != null) {
            user.updateNotificationDraftThreshold(normalizeDraftThreshold(draftThreshold));
        }

        user.updateNotificationConfigs(current);
        return new NotificationSettingsResponse(current, normalizeDraftThreshold(user.getNotificationDraftThreshold()));
    }

    // =============================================
    // 알림 생성 (비즈니스 이벤트 트리거용)
    // 유저 설정에서 해당 type이 true인 경우에만 INSERT
    // 호출자 트랜잭션에 참여 (REQUIRED)
    // =============================================
    @Transactional
    public void createNotification(User user, NotificationType type, String title, String notiMessage, Long relatedId) {
        if (!isNotificationEnabled(user, type)) {
            return;
        }
        Notification notification = notificationRepository.save(Notification.builder()
                .user(user)
                .type(type.name())
                .title(title)
                .notiMessage(notiMessage)
                .relatedId(relatedId)
                .build());

        publishNotificationUpdated(user.getUserId(), notification);
    }

    @Transactional
    public void createPendingDraftQueueNotificationIfNeeded(User user, Long relatedId) {
        if (!isNotificationEnabled(user, NotificationType.DRAFT_PENDING)) {
            return;
        }

        long pendingCount = draftReplyRepository.countByUser_UserIdAndStatus(
                user.getUserId(),
                DraftStatus.PENDING_REVIEW
        ) + recommendationRepository.countPendingReviewRecommendationsWithoutDraft(user.getUserId());

        int threshold = normalizeDraftThreshold(user.getNotificationDraftThreshold());
        if (pendingCount < threshold) {
            return;
        }

        if (relatedId != null && notificationRepository.existsByUser_UserIdAndTypeAndRelatedIdAndIsReadFalse(
                user.getUserId(),
                NotificationType.DRAFT_PENDING.name(),
                relatedId
        )) {
            return;
        }

        createNotification(
                user,
                NotificationType.DRAFT_PENDING,
                "검토 대기 초안이 쌓였습니다",
                pendingCount + "개의 검토 대기 초안이 있습니다. 수신함에서 확인해 주세요.",
                relatedId
        );
    }

    @Transactional
    public void createAutoSendSummaryIfNeeded(User user, LocalDate summaryDate) {
        if (!isNotificationEnabled(user, NotificationType.AUTO_SEND_SUMMARY)) {
            return;
        }

        LocalDateTime start = summaryDate.atStartOfDay();
        LocalDateTime end = summaryDate.plusDays(1).atStartOfDay();
        if (notificationRepository.existsByUser_UserIdAndTypeAndCreatedAtBetween(
                user.getUserId(),
                NotificationType.AUTO_SEND_SUMMARY.name(),
                start,
                end
        )) {
            return;
        }

        long sentCount = draftReplyRepository.countByUser_UserIdAndStatusInAndUpdatedAtBetween(
                user.getUserId(),
                List.of(DraftStatus.SENT, DraftStatus.EDITED),
                start,
                end
        );

        if (sentCount <= 0) {
            return;
        }

        createNotification(
                user,
                NotificationType.AUTO_SEND_SUMMARY,
                "오늘 답장 처리 요약",
                "오늘 " + sentCount + "개의 답장을 처리했습니다.",
                null
        );
    }

    private Boolean toBoolean(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            if ("true".equalsIgnoreCase(text)) {
                return true;
            }
            if ("false".equalsIgnoreCase(text)) {
                return false;
            }
        }
        return null;
    }

    private Integer toInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        if (value instanceof String text) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private int normalizeDraftThreshold(Integer threshold) {
        if (threshold == null) {
            return DEFAULT_DRAFT_THRESHOLD;
        }
        return Math.max(MIN_DRAFT_THRESHOLD, Math.min(MAX_DRAFT_THRESHOLD, threshold));
    }

    private boolean isNotificationEnabled(User user, NotificationType type) {
        Map<String, Boolean> configs = user.getNotificationConfigs();
        // configs null = 기존 유저 마이그레이션 전 상태 → 기본값(전체 허용)으로 처리
        return configs == null || !Boolean.FALSE.equals(configs.get(type.name()));
    }

    private void publishNotificationUpdated(Long userId, Notification notification) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("notification_id", notification.getNotificationId());
        payload.put("type", notification.getType());
        payload.put("title", notification.getTitle());
        payload.put("related_id", notification.getRelatedId());

        eventPublisher.publishEvent(new SseEvent(this, userId, "notification-updated", payload));
    }
}
