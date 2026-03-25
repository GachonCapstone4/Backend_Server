package com.emailagent.repository;

import com.emailagent.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 전체 알림 조회 (최신순)
    List<Notification> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // 미확인 알림만 조회 (최신순)
    List<Notification> findByUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    // 소유권 검증 포함 단건 조회
    Optional<Notification> findByNotificationIdAndUser_UserId(Long notificationId, Long userId);

    // 전체 읽음 처리 (벌크 UPDATE — 1건씩 dirty checking 대신 단일 쿼리 사용)
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);
}
