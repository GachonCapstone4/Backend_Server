package com.emailagent.service;

import com.emailagent.domain.entity.Category;
import com.emailagent.domain.entity.Email;
import com.emailagent.domain.entity.EmailTemplateRecommendation;
import com.emailagent.domain.entity.Template;
import com.emailagent.domain.entity.User;
import com.emailagent.domain.enums.TemplateOrigin;
import com.emailagent.rabbitmq.dto.RagTemplateMatchResultDTO;
import com.emailagent.rabbitmq.publisher.RagPublisher;
import com.emailagent.repository.BusinessProfileRepository;
import com.emailagent.repository.CategoryRepository;
import com.emailagent.repository.EmailRepository;
import com.emailagent.repository.EmailTemplateRecommendationRepository;
import com.emailagent.repository.TemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RagResultServiceTest {

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BusinessProfileRepository profileRepository;

    @Mock
    private EmailRepository emailRepository;

    @Mock
    private EmailTemplateRecommendationRepository recommendationRepository;

    @Mock
    private TemplateNumberService templateNumberService;

    @Mock
    private RagTemplateIndexService ragTemplateIndexService;

    @Mock
    private RagPublisher ragPublisher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RagResultService ragResultService;

    @Test
    void handleTemplateMatchedSkipsDeletedTemplateIds() {
        Long userId = 70L;
        Long emailId = 500L;
        User user = User.builder()
                .userId(userId)
                .email("user@example.com")
                .password("encoded")
                .name("사용자")
                .build();
        Email email = Email.builder()
                .emailId(emailId)
                .user(user)
                .externalMsgId("gmail-500")
                .senderEmail("customer@example.com")
                .subject("가격 문의")
                .bodyClean("가격을 알려주세요.")
                .receivedAt(LocalDateTime.now())
                .build();
        Category category = Category.builder()
                .categoryId(10L)
                .user(user)
                .categoryName("가격 문의")
                .build();
        Template existingTemplate = Template.builder()
                .templateId(100L)
                .user(user)
                .category(category)
                .origin(TemplateOrigin.AI_GENERATED)
                .userModified(false)
                .title("가격 안내")
                .subjectTemplate("가격 안내")
                .bodyTemplate("본문")
                .build();
        RagTemplateMatchResultDTO result = matchResult(
                userId,
                emailId,
                List.of(matchItem(999L, 0.97), matchItem(100L, 0.91))
        );

        when(emailRepository.findByEmailIdAndUserId(emailId, userId)).thenReturn(Optional.of(email));
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());
        when(templateRepository.findById(100L)).thenReturn(Optional.of(existingTemplate));

        ragResultService.handleTemplateMatched(result);

        verify(recommendationRepository).deleteByUser_UserIdAndEmail_EmailId(userId, emailId);
        ArgumentCaptor<EmailTemplateRecommendation> recommendation =
                ArgumentCaptor.forClass(EmailTemplateRecommendation.class);
        verify(recommendationRepository).save(recommendation.capture());
        assertThat(recommendation.getValue().getTemplate()).isEqualTo(existingTemplate);
        assertThat(recommendation.getValue().getRankOrder()).isEqualTo(1);
        verify(notificationService).createPendingDraftQueueNotificationIfNeeded(user, emailId);
    }

    @Test
    void handleTemplateMatchedDoesNotSaveWhenAllMatchedTemplatesAreDeleted() {
        Long userId = 70L;
        Long emailId = 500L;
        User user = User.builder()
                .userId(userId)
                .email("user@example.com")
                .password("encoded")
                .name("사용자")
                .build();
        Email email = Email.builder()
                .emailId(emailId)
                .user(user)
                .externalMsgId("gmail-500")
                .senderEmail("customer@example.com")
                .subject("가격 문의")
                .bodyClean("가격을 알려주세요.")
                .receivedAt(LocalDateTime.now())
                .build();
        RagTemplateMatchResultDTO result = matchResult(
                userId,
                emailId,
                List.of(matchItem(999L, 0.97))
        );

        when(emailRepository.findByEmailIdAndUserId(emailId, userId)).thenReturn(Optional.of(email));
        when(templateRepository.findById(999L)).thenReturn(Optional.empty());

        ragResultService.handleTemplateMatched(result);

        verify(recommendationRepository).deleteByUser_UserIdAndEmail_EmailId(userId, emailId);
        verify(recommendationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(notificationService, never()).createPendingDraftQueueNotificationIfNeeded(user, emailId);
    }

    private RagTemplateMatchResultDTO matchResult(
            Long userId,
            Long emailId,
            List<RagTemplateMatchResultDTO.ResultItem> items
    ) {
        RagTemplateMatchResultDTO.Payload payload = new RagTemplateMatchResultDTO.Payload();
        ReflectionTestUtils.setField(payload, "emailId", String.valueOf(emailId));
        ReflectionTestUtils.setField(payload, "results", items);

        RagTemplateMatchResultDTO result = new RagTemplateMatchResultDTO();
        ReflectionTestUtils.setField(result, "jobId", "template-match-" + emailId);
        ReflectionTestUtils.setField(result, "requestId", "template-match-" + emailId);
        ReflectionTestUtils.setField(result, "userId", userId);
        ReflectionTestUtils.setField(result, "status", "SUCCESS");
        ReflectionTestUtils.setField(result, "payload", payload);
        return result;
    }

    private RagTemplateMatchResultDTO.ResultItem matchItem(Long templateId, Double score) {
        RagTemplateMatchResultDTO.ResultItem item = new RagTemplateMatchResultDTO.ResultItem();
        ReflectionTestUtils.setField(item, "templateId", templateId);
        ReflectionTestUtils.setField(item, "score", score);
        return item;
    }
}
