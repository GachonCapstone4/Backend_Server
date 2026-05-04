package com.emailagent.service;

import com.emailagent.domain.entity.BusinessProfile;
import com.emailagent.domain.entity.Category;
import com.emailagent.domain.entity.Template;
import com.emailagent.domain.entity.User;
import com.emailagent.domain.enums.EmailTone;
import com.emailagent.domain.enums.TemplateOrigin;
import com.emailagent.dto.request.business.CategoryRequest;
import com.emailagent.dto.request.business.TemplateRegenerateRequest;
import com.emailagent.dto.response.business.CategoryResponse;
import com.emailagent.dto.response.business.TemplateRegenerateResponse;
import com.emailagent.rag.application.RagIntegrationService;
import com.emailagent.repository.AutomationRuleRepository;
import com.emailagent.repository.BusinessFaqRepository;
import com.emailagent.repository.BusinessProfileRepository;
import com.emailagent.repository.BusinessResourceRepository;
import com.emailagent.repository.CategoryRepository;
import com.emailagent.repository.DraftReplyRepository;
import com.emailagent.repository.EmailTemplateRecommendationRepository;
import com.emailagent.repository.TemplateRepository;
import com.emailagent.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusinessServiceTest {

    @Mock
    private BusinessProfileRepository profileRepository;

    @Mock
    private BusinessResourceRepository resourceRepository;

    @Mock
    private BusinessFaqRepository faqRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TemplateRepository templateRepository;

    @Mock
    private AutomationRuleRepository automationRuleRepository;

    @Mock
    private DraftReplyRepository draftReplyRepository;

    @Mock
    private EmailTemplateRecommendationRepository recommendationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private RagTemplateIndexService ragTemplateIndexService;

    @Mock
    private RagIntegrationService ragIntegrationService;

    @InjectMocks
    private BusinessService businessService;

    @Test
    void createCategoryUsesUserReferenceWithoutLoadingUserEntity() {
        Long userId = 70L;
        User userReference = User.builder()
                .userId(userId)
                .email("user@example.com")
                .password("encoded")
                .name("사용자")
                .build();
        CategoryRequest request = new CategoryRequest();
        ReflectionTestUtils.setField(request, "categoryName", "가격 문의");
        ReflectionTestUtils.setField(request, "color", "#2563EB");

        when(categoryRepository.existsByUser_UserIdAndCategoryName(userId, "가격 문의"))
                .thenReturn(false);
        when(userRepository.existsById(userId)).thenReturn(true);
        when(userRepository.getReferenceById(userId)).thenReturn(userReference);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            return Category.builder()
                    .categoryId(10L)
                    .user(userReference)
                    .categoryName(category.getCategoryName())
                    .color(category.getColor())
                    .build();
        });

        CategoryResponse response = businessService.createCategory(userId, request);

        assertThat(response.getCategoryId()).isEqualTo(10L);
        assertThat(response.getCategoryName()).isEqualTo("가격 문의");
        verify(userRepository, never()).findById(userId);
    }

    @Test
    void regenerateTemplatesReturnsDraftJobIds() {
        Long userId = 70L;
        User user = User.builder()
                .userId(userId)
                .email("user@example.com")
                .password("encoded")
                .name("사용자")
                .build();
        Category category = Category.builder()
                .categoryId(10L)
                .user(user)
                .categoryName("가격 문의")
                .build();
        BusinessProfile profile = BusinessProfile.builder()
                .user(user)
                .industryType("Sales")
                .emailTone(EmailTone.NEUTRAL)
                .companyDescription("회사 소개")
                .build();
        Template template = Template.builder()
                .templateId(100L)
                .user(user)
                .category(category)
                .origin(TemplateOrigin.AI_GENERATED)
                .userModified(false)
                .title("가격 안내")
                .subjectTemplate("가격 안내")
                .bodyTemplate("본문")
                .build();
        TemplateRegenerateRequest request = new TemplateRegenerateRequest();
        ReflectionTestUtils.setField(request, "regenerateAll", false);
        ReflectionTestUtils.setField(request, "templateIds", List.of(100L));

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(templateRepository.findAllById(List.of(100L))).thenReturn(List.of(template));
        when(faqRepository.findByUser_UserId(userId)).thenReturn(List.of());
        when(ragIntegrationService.requestTemplateRegenerationDrafts(any(), any(), any(), any()))
                .thenReturn(List.of("rag-draft-70-10-request"));

        TemplateRegenerateResponse response = businessService.regenerateTemplates(userId, request);

        assertThat(response.getProcessingCount()).isEqualTo(1);
        assertThat(response.getJobIds()).containsExactly("rag-draft-70-10-request");
    }

    @Test
    void regenerateTemplatesDeletesOnlyUnmodifiedAiGeneratedTemplates() {
        Long userId = 70L;
        User user = User.builder()
                .userId(userId)
                .email("user@example.com")
                .password("encoded")
                .name("사용자")
                .build();
        Category category = Category.builder()
                .categoryId(10L)
                .user(user)
                .categoryName("가격 문의")
                .build();
        BusinessProfile profile = BusinessProfile.builder()
                .user(user)
                .industryType("Sales")
                .emailTone(EmailTone.NEUTRAL)
                .companyDescription("회사 소개")
                .build();
        Template generatedTemplate = Template.builder()
                .templateId(100L)
                .user(user)
                .category(category)
                .origin(TemplateOrigin.AI_GENERATED)
                .userModified(false)
                .title("가격 안내")
                .subjectTemplate("가격 안내")
                .bodyTemplate("본문")
                .build();
        Template editedTemplate = Template.builder()
                .templateId(101L)
                .user(user)
                .category(category)
                .origin(TemplateOrigin.AI_GENERATED)
                .userModified(true)
                .title("편집한 가격 안내")
                .subjectTemplate("편집한 가격 안내")
                .bodyTemplate("본문")
                .build();
        Template userCreatedTemplate = Template.builder()
                .templateId(102L)
                .user(user)
                .category(category)
                .origin(TemplateOrigin.USER_CREATED)
                .userModified(false)
                .title("직접 만든 가격 안내")
                .subjectTemplate("직접 만든 가격 안내")
                .bodyTemplate("본문")
                .build();
        TemplateRegenerateRequest request = new TemplateRegenerateRequest();
        ReflectionTestUtils.setField(request, "regenerateAll", true);
        ReflectionTestUtils.setField(request, "templateIds", List.of());

        when(profileRepository.findByUser_UserId(userId)).thenReturn(Optional.of(profile));
        when(templateRepository.findByUser_UserId(userId))
                .thenReturn(List.of(generatedTemplate, editedTemplate, userCreatedTemplate));
        when(faqRepository.findByUser_UserId(userId)).thenReturn(List.of());
        when(ragIntegrationService.requestTemplateRegenerationDrafts(any(), any(), any(), any()))
                .thenReturn(List.of("rag-draft-70-10-request"));

        TemplateRegenerateResponse response = businessService.regenerateTemplates(userId, request);

        assertThat(response.getProcessingCount()).isEqualTo(1);
        assertThat(response.getJobIds()).containsExactly("rag-draft-70-10-request");

        verify(ragTemplateIndexService).deleteTemplateIndexes(userId, List.of(100L));
        verify(recommendationRepository).deleteByTemplate_TemplateIdIn(List.of(100L));
        verify(automationRuleRepository).clearTemplateReferences(List.of(100L));
        verify(draftReplyRepository).clearTemplateReferences(List.of(100L));

        verify(templateRepository).deleteAll(List.of(generatedTemplate));
    }
}
