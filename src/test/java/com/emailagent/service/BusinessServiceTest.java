package com.emailagent.service;

import com.emailagent.domain.entity.Category;
import com.emailagent.domain.entity.User;
import com.emailagent.dto.request.business.CategoryRequest;
import com.emailagent.dto.response.business.CategoryResponse;
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
}
