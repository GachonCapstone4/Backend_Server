package com.emailagent.controller;

import com.emailagent.dto.response.auth.BaseResponse;
import com.emailagent.dto.response.notification.NotificationResponse;
import com.emailagent.dto.response.notification.NotificationSettingsResponse;
import com.emailagent.security.CurrentUser;
import com.emailagent.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // GET /api/notifications?is_read=false
    @GetMapping
    public ResponseEntity<NotificationResponse.ListResponse> getNotifications(
            @CurrentUser Long userId,
            @RequestParam(name = "is_read", required = false) Boolean isRead) {
        return ResponseEntity.ok(notificationService.getNotifications(userId, isRead));
    }

    // PATCH /api/notifications/{notification_id}/read
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<BaseResponse> markAsRead(
            @CurrentUser Long userId,
            @PathVariable Long notificationId) {
        return ResponseEntity.ok(notificationService.markAsRead(userId, notificationId));
    }

    // PATCH /api/notifications/read-all
    @PatchMapping("/read-all")
    public ResponseEntity<BaseResponse> markAllAsRead(@CurrentUser Long userId) {
        return ResponseEntity.ok(notificationService.markAllAsRead(userId));
    }

    // GET /api/notifications/settings
    @GetMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> getSettings(@CurrentUser Long userId) {
        return ResponseEntity.ok(notificationService.getSettings(userId));
    }

    // PATCH /api/notifications/settings
    @PatchMapping("/settings")
    public ResponseEntity<NotificationSettingsResponse> updateSettings(
            @CurrentUser Long userId,
            @RequestBody Map<String, Object> settings) {
        return ResponseEntity.ok(notificationService.updateSettings(userId, settings));
    }
}
