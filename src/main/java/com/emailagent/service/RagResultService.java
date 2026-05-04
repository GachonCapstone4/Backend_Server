package com.emailagent.service;

import com.emailagent.domain.entity.AutomationRule;
import com.emailagent.domain.entity.Category;
import com.emailagent.domain.entity.DraftReply;
import com.emailagent.domain.entity.Email;
import com.emailagent.domain.entity.EmailAnalysisResult;
import com.emailagent.domain.entity.EmailTemplateRecommendation;
import com.emailagent.domain.entity.Template;
import com.emailagent.domain.enums.DraftStatus;
import com.emailagent.domain.enums.EmailStatus;
import com.emailagent.domain.enums.NotificationType;
import com.emailagent.domain.enums.TemplateOrigin;
import com.emailagent.exception.ResourceNotFoundException;
import com.emailagent.rabbitmq.dto.RagDraftGenerateResultDTO;
import com.emailagent.rabbitmq.dto.RagTemplateMatchResultDTO;
import com.emailagent.rabbitmq.dto.RagTemplateIndexRequestDTO;
import com.emailagent.rabbitmq.event.SseEvent;
import com.emailagent.rabbitmq.publisher.RagPublisher;
import com.emailagent.repository.AutomationRuleRepository;
import com.emailagent.repository.BusinessProfileRepository;
import com.emailagent.repository.CategoryRepository;
import com.emailagent.repository.DraftReplyRepository;
import com.emailagent.repository.EmailAnalysisResultRepository;
import com.emailagent.repository.EmailRepository;
import com.emailagent.repository.EmailTemplateRecommendationRepository;
import com.emailagent.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RAG 결과 메시지를 백엔드 도메인 모델에 반영하는 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagResultService {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");
    private static final DateTimeFormatter TEMPLATE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final TemplateRepository templateRepository;
    private final CategoryRepository categoryRepository;
    private final BusinessProfileRepository profileRepository;
    private final EmailRepository emailRepository;
    private final EmailTemplateRecommendationRepository recommendationRepository;
    private final TemplateNumberService templateNumberService;
    private final RagTemplateIndexService ragTemplateIndexService;
    private final RagPublisher ragPublisher;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final DraftReplyRepository draftReplyRepository;
    private final AutomationRuleRepository automationRuleRepository;
    private final GmailApiService gmailApiService;
    private final EmailAnalysisResultRepository analysisResultRepository;

    @Transactional
    public void handleDraftGenerated(RagDraftGenerateResultDTO result) {
        if (!"SUCCESS".equalsIgnoreCase(result.getStatus())) {
            String message = result.getError() != null ? result.getError().getMessage() : "RAG draft 생성 실패";
            log.warn(
                    "[RagResultService] draft result 실패 응답 수신 — requestId={}, jobId={}, message={}",
                    result.getRequestId(),
                    result.getJobId(),
                    message
            );
            return;
        }

        RagDraftGenerateResultDTO.Payload payload = result.getPayload();
        if (payload == null) {
            throw new IllegalArgumentException("RAG draft 결과 payload가 없습니다.");
        }

        Long userId = result.getUserId();
        Long categoryId = payload.getCategoryId();

        Category category = categoryRepository.findById(categoryId)
                .filter(found -> found.getUser().getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다: " + categoryId));

        List<RagDraftGenerateResultDTO.TemplateItem> items = payload.getTemplates();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("RAG draft 결과 payload.templates가 없습니다.");
        }

        List<Template> savedTemplates = items.stream()
                .map(item -> upsertTemplate(userId, category, item))
                .toList();

        publishTemplateIndex(savedTemplates, category, items);

        // 같은 category_name을 공유하는 모든 템플릿의 user_count 갱신
        templateRepository.findUserCountPerTemplate().forEach(row -> {
            Long templateId = ((Number) row[0]).longValue();
            int count = ((Number) row[1]).intValue();
            templateRepository.findById(templateId).ifPresent(t -> t.updateUserCount(count));
        });

        log.info(
                "[RagResultService] draft 결과로 템플릿 저장 완료 — userId={}, categoryId={}, count={}",
                userId,
                categoryId,
                savedTemplates.size()
        );
    }

    private Template upsertTemplate(Long userId, Category category, RagDraftGenerateResultDTO.TemplateItem item) {
        Template template = templateRepository
                .findFirstByUser_UserIdAndCategory_CategoryIdAndVariantLabelAndOriginAndUserModifiedFalseOrderByCreatedAtDesc(
                        userId,
                        category.getCategoryId(),
                        item.getVariantLabel(),
                        TemplateOrigin.AI_GENERATED
                )
                .map(existing -> {
                    existing.update(
                            item.getTitle(),
                            item.getVariantLabel(),
                            item.getSubjectTemplate(),
                            item.getBodyTemplate()
                    );
                    existing.markAiGenerated();
                    return existing;
                })
                .orElseGet(() -> Template.builder()
                        .userTemplateNo(templateNumberService.nextUserTemplateNo(userId))
                        .user(category.getUser())
                        .category(category)
                        .title(item.getTitle())
                        .variantLabel(item.getVariantLabel())
                        .subjectTemplate(item.getSubjectTemplate())
                        .bodyTemplate(item.getBodyTemplate())
                        .build());
        template.markAiGenerated();

        return templateRepository.save(template);
    }

    private void publishTemplateIndex(
            List<Template> templates,
            Category category,
            List<RagDraftGenerateResultDTO.TemplateItem> generatedItems
    ) {
        String requestId = "template-index-" + category.getCategoryId() + "-" + System.currentTimeMillis();
        String emailTone = profileRepository.findByUser_UserId(category.getUser().getUserId())
                .map(profile -> profile.getEmailTone() != null ? profile.getEmailTone().name() : null)
                .orElse(null);

        List<RagTemplateIndexRequestDTO.TemplateItem> indexItems = templates.stream()
                .map(template -> {
                    RagDraftGenerateResultDTO.TemplateItem generated = generatedItems.stream()
                            .filter(item -> item.getVariantLabel().equals(template.getVariantLabel()))
                            .findFirst()
                            .orElse(null);

                    String variantLabel = template.getVariantLabel() != null ? template.getVariantLabel() : "일반형";
                    String templatePurpose = generated != null ? generated.getTemplatePurpose() : null;
                    String canonicalText = buildCanonicalText(template, category, emailTone, variantLabel, templatePurpose);
                    template.prepareIndexing(canonicalText);
                    return RagTemplateIndexRequestDTO.TemplateItem.builder()
                            .templateId(template.getTemplateId())
                            .title(template.getTitle())
                            .categoryName(category.getCategoryName())
                            .emailTone(emailTone)
                            .canonicalText(canonicalText)
                            .metadata(
                                    RagTemplateIndexRequestDTO.Metadata.builder()
                                            .templatePurpose(templatePurpose)
                                            .searchSummary(variantLabel + " 템플릿")
                                            .semanticKeywords(ragTemplateIndexService.toSemanticKeywords(category, variantLabel))
                                            .recommendedSituations(templatePurpose != null ? List.of(templatePurpose) : List.of())
                                            .build()
                            )
                            .build();
                })
                .toList();

        RagTemplateIndexRequestDTO message = RagTemplateIndexRequestDTO.builder()
                .requestId(requestId)
                .userId(category.getUser().getUserId())
                .payload(
                        RagTemplateIndexRequestDTO.Payload.builder()
                                .templates(indexItems)
                                .deleteTemplateIds(List.of())
                                .build()
                )
                .build();

        ragPublisher.publishTemplateIndex(message);
    }

    private String buildCanonicalText(
            Template template,
            Category category,
            String emailTone,
            String variantLabel,
            String templatePurpose
    ) {
        return """
                제목: %s
                카테고리: %s
                유형: %s
                목적: %s
                어조: %s
                메일 제목: %s
                본문:
                %s
                """.formatted(
                template.getTitle(),
                category.getCategoryName(),
                variantLabel,
                templatePurpose != null ? templatePurpose : "미지정",
                emailTone != null ? emailTone : "미지정",
                template.getSubjectTemplate(),
                template.getBodyTemplate()
        ).trim();
    }

    @Transactional
    public void handleTemplateMatched(RagTemplateMatchResultDTO result) {
        if (!"SUCCESS".equalsIgnoreCase(result.getStatus())) {
            String message = result.getError() != null ? result.getError().getMessage() : "RAG template match 실패";
            log.warn(
                    "[RagResultService] template match 실패 응답 수신 — requestId={}, jobId={}, message={}",
                    result.getRequestId(),
                    result.getJobId(),
                    message
            );
            return;
        }

        RagTemplateMatchResultDTO.Payload payload = result.getPayload();
        if (payload == null || payload.getEmailId() == null) {
            throw new IllegalArgumentException("RAG template match 결과 payload가 올바르지 않습니다.");
        }

        Long userId = result.getUserId();
        Long emailId = parseEmailId(payload.getEmailId());
        Email email = emailRepository.findByEmailIdAndUserId(emailId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("이메일을 찾을 수 없습니다: " + emailId));

        recommendationRepository.deleteByUser_UserIdAndEmail_EmailId(userId, emailId);

        List<RagTemplateMatchResultDTO.ResultItem> items = payload.getResults();
        if (items == null || items.isEmpty()) {
            log.info(
                    "[RagResultService] template match 결과가 비어있습니다 — userId={}, emailId={}",
                    userId,
                    emailId
            );
            pushTemplateMatchUpdate(userId, emailId, 0);
            return;
        }

        int rank = 1;
        int savedCount = 0;
        Template topTemplate = null;
        for (RagTemplateMatchResultDTO.ResultItem item : items) {
            Long templateId = item.getTemplateId();
            Template template = templateRepository.findById(templateId)
                    .filter(found -> found.getUser().getUserId().equals(userId))
                    .orElse(null);

            if (template == null) {
                log.warn(
                        "[RagResultService] 존재하지 않는 추천 템플릿 제외 — userId={}, emailId={}, templateId={}",
                        userId,
                        emailId,
                        templateId
                );
                continue;
            }

            EmailTemplateRecommendation recommendation = EmailTemplateRecommendation.builder()
                    .user(email.getUser())
                    .email(email)
                    .template(template)
                    .score(item.getScore())
                    .rankOrder(rank++)
                    .build();

            recommendationRepository.save(recommendation);
            savedCount++;
            if (topTemplate == null) topTemplate = template;
            rank++;
        }

        log.info(
                "[RagResultService] template match 결과 저장 완료 — userId={}, emailId={}, count={}",
                userId,
                emailId,
                savedCount
        );

        if (savedCount > 0) {
            notificationService.createPendingDraftQueueNotificationIfNeeded(email.getUser(), emailId);
            saveDraftAndTriggerAutoSend(email, userId, emailId, topTemplate);
        }
        pushTemplateMatchUpdate(userId, emailId, savedCount);
    }

    /**
     * 최상위 추천 템플릿으로 DraftReply를 저장하고, AutomationRule 조건 충족 시 자동 발송을 시도한다.
     * placeholder가 미완성인 경우 발송 없이 AUTO_SEND_FAILED 알림만 발송한다.
     */
    private void saveDraftAndTriggerAutoSend(Email email, Long userId, Long emailId, Template topTemplate) {
        // 변수 맵 구성
        EmailAnalysisResult analysisResult = analysisResultRepository.findByEmail_EmailId(emailId).orElse(null);
        Map<String, Object> variables = buildAutoSendVariables(email, analysisResult);

        // placeholder 치환 (미완성 항목은 {{key}} 그대로 유지)
        String filledSubject = applyTemplateVariables(topTemplate.getSubjectTemplate(), variables);
        String filledBody = applyTemplateVariables(topTemplate.getBodyTemplate(), variables);

        // DraftReply upsert (PENDING_REVIEW)
        DraftReply draft = draftReplyRepository.findByEmailIdAndUserId(emailId, userId)
                .map(existing -> {
                    existing.updateContent(filledSubject, filledBody);
                    existing.updateTemplate(topTemplate);
                    existing.updateStatus(DraftStatus.PENDING_REVIEW);
                    return existing;
                })
                .orElseGet(() -> draftReplyRepository.save(DraftReply.builder()
                        .user(email.getUser())
                        .email(email)
                        .template(topTemplate)
                        .status(DraftStatus.PENDING_REVIEW)
                        .draftSubject(filledSubject)
                        .draftContent(filledBody)
                        .build()));
        log.info("[RagResultService] draft 저장 완료 — emailId={}, templateId={}", emailId, topTemplate.getTemplateId());

        // 자동발송 규칙 매칭: autoSendEnabled=true, isActive=true, template == topTemplate
        boolean shouldAutoSend = automationRuleRepository.findByUserIdWithDetails(userId).stream()
                .anyMatch(rule -> rule.isAutoSendEnabled()
                        && rule.isActive()
                        && rule.getTemplate() != null
                        && rule.getTemplate().getTemplateId().equals(topTemplate.getTemplateId()));

        if (!shouldAutoSend) return;

        // 미완성 placeholder 존재 여부 확인
        boolean hasUnfilled = PLACEHOLDER_PATTERN.matcher(filledSubject).find()
                || PLACEHOLDER_PATTERN.matcher(filledBody).find();

        if (hasUnfilled) {
            notificationService.createNotification(
                    email.getUser(),
                    NotificationType.AUTO_SEND_FAILED,
                    "자동 발송 실패",
                    "템플릿 내 일부 항목이 자동으로 채워지지 않아 발송하지 못했습니다.",
                    emailId
            );
            log.info("[RagResultService] 자동 발송 실패 (미완성 placeholder) — emailId={}, templateId={}",
                    emailId, topTemplate.getTemplateId());
            return;
        }

        // 자동 발송
        gmailApiService.sendEmail(userId, email.getSenderEmail(), filledSubject, filledBody);
        email.updateStatus(EmailStatus.PROCESSED);
        draft.updateStatus(DraftStatus.SENT);
        log.info("[RagResultService] 자동 발송 완료 — emailId={}, templateId={}", emailId, topTemplate.getTemplateId());
    }

    private Map<String, Object> buildAutoSendVariables(Email email, EmailAnalysisResult analysisResult) {
        Map<String, Object> variables = new LinkedHashMap<>();

        // 고객명: senderName 우선, 없으면 이메일 로컬파트
        String customerName = (email.getSenderName() != null && !email.getSenderName().isBlank())
                ? email.getSenderName().trim()
                : extractEmailLocalPart(email.getSenderEmail());
        variables.put("고객명", customerName);

        if (analysisResult != null) {
            if (analysisResult.getIntent() != null) variables.put("문의주제", analysisResult.getIntent());
            if (analysisResult.getSummaryText() != null) variables.put("문의요약", analysisResult.getSummaryText());
        }
        if (email.getReceivedAt() != null) {
            variables.put("수신일자", TEMPLATE_DATE_FORMATTER.format(email.getReceivedAt()));
        }
        String userName = email.getUser().getName();
        if (userName != null && !userName.isBlank()) variables.put("담당자명", userName);

        return variables;
    }

    private String applyTemplateVariables(String templateText, Map<String, Object> variables) {
        if (templateText == null) return "";
        StringBuffer result = new StringBuffer();
        Matcher m = PLACEHOLDER_PATTERN.matcher(templateText);
        while (m.find()) {
            String key = m.group(1).trim();
            Object val = variables.get(key);
            // 값이 없으면 원본 {{key}} 그대로 유지
            m.appendReplacement(result, Matcher.quoteReplacement(
                    val != null ? val.toString() : m.group(0)
            ));
        }
        m.appendTail(result);
        return result.toString();
    }

    private String extractEmailLocalPart(String emailAddr) {
        if (emailAddr == null) return "";
        int atIdx = emailAddr.indexOf('@');
        return atIdx > 0 ? emailAddr.substring(0, atIdx) : emailAddr;
    }

    private Long parseEmailId(String rawEmailId) {
        try {
            return Long.parseLong(rawEmailId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("emailId는 숫자여야 합니다: " + rawEmailId, e);
        }
    }

    private void pushTemplateMatchUpdate(Long userId, Long emailId, int recommendationCount) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("email_id", emailId);
        payload.put("recommendation_count", recommendationCount);

        eventPublisher.publishEvent(new SseEvent(this, userId, "template-match-updated", payload));
    }
}
