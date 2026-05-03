package com.emailagent.scheduler;

import com.emailagent.repository.UserRepository;
import com.emailagent.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSummaryScheduler {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 18 * * *", zone = "Asia/Seoul")
    public void createDailyAutoSendSummaries() {
        LocalDate today = LocalDate.now();
        userRepository.findByIsActiveTrue().forEach(user -> {
            try {
                notificationService.createAutoSendSummaryIfNeeded(user, today);
            } catch (Exception e) {
                log.warn(
                        "[NotificationSummaryScheduler] 자동 발송 요약 알림 생성 실패 — userId={}, error={}",
                        user.getUserId(),
                        e.getMessage(),
                        e
                );
            }
        });
    }
}
