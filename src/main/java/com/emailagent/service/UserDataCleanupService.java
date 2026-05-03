package com.emailagent.service;

import com.emailagent.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 탈퇴 계정 재가입 시 기존 데이터를 초기화한다.
 * FK 제약 위반 방지를 위해 하위 엔티티(Email 참조)부터 상위 엔티티 순으로 삭제한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserDataCleanupService {

    private final OutboxRepository outboxRepository;
    private final EmailAnalysisResultRepository emailAnalysisResultRepository;
    private final DraftReplyRepository draftReplyRepository;
    private final EmailTemplateRecommendationRepository emailTemplateRecommendationRepository;
    private final CalendarEventRepository calendarEventRepository;
    private final EmailRepository emailRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final TemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;
    private final NotificationRepository notificationRepository;
    private final RagJobRepository ragJobRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final BusinessFaqRepository businessFaqRepository;
    private final BusinessResourceRepository businessResourceRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final IntegrationRepository integrationRepository;

    public void clearAllUserData(Long userId) {
        log.info("[재가입] 탈퇴 계정 데이터 초기화 시작: userId={}", userId);

        // 1단계: Email의 하위 엔티티 — Email 삭제 전 먼저 처리 (FK: email_id)
        outboxRepository.deleteByUserId(userId);
        emailAnalysisResultRepository.deleteByUserId(userId);
        draftReplyRepository.deleteByUser_UserId(userId);
        emailTemplateRecommendationRepository.deleteByUser_UserId(userId);
        calendarEventRepository.deleteByUser_UserId(userId);

        // 2단계: Email 삭제 — 하위 엔티티가 모두 정리된 후
        emailRepository.deleteByUser_UserId(userId);

        // 3단계: AutomationRule — Category/Template 참조 (FK: category_id, template_id)
        automationRuleRepository.deleteByUser_UserId(userId);

        // 4단계: Template — Category 참조 (FK: category_id), Category cascade 전 먼저 처리
        templateRepository.deleteByUser_UserId(userId);

        // 5단계: 나머지 User 직접 참조 엔티티
        categoryRepository.deleteByUser_UserId(userId);
        notificationRepository.deleteByUser_UserId(userId);
        ragJobRepository.deleteByUser_UserId(userId);
        supportTicketRepository.deleteByUser_UserId(userId);
        businessFaqRepository.deleteByUser_UserId(userId);
        businessResourceRepository.deleteByUser_UserId(userId);
        businessProfileRepository.deleteByUser_UserId(userId);
        integrationRepository.deleteByUser_UserId(userId);

        log.info("[재가입] 탈퇴 계정 데이터 초기화 완료: userId={}", userId);
    }
}
