package com.emailagent.service;

import com.emailagent.domain.entity.Category;
import com.emailagent.domain.entity.CategoryKeywordRule;
import com.emailagent.domain.entity.Template;
import com.emailagent.rabbitmq.dto.RagTemplateIndexRequestDTO;
import com.emailagent.rabbitmq.publisher.RagPublisher;
import com.emailagent.repository.BusinessProfileRepository;
import com.emailagent.repository.CategoryKeywordRuleRepository;
import com.emailagent.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RagTemplateIndexService {

    private final TemplateRepository templateRepository;
    private final BusinessProfileRepository profileRepository;
    private final CategoryKeywordRuleRepository keywordRuleRepository;
    private final RagPublisher ragPublisher;

    public void reindexCategories(List<Category> categories) {
        Map<Long, Category> uniqueCategories = new LinkedHashMap<>();
        categories.forEach(category -> uniqueCategories.put(category.getCategoryId(), category));
        uniqueCategories.values().forEach(this::reindexCategory);
    }

    public void reindexTemplate(Template template) {
        reindexTemplates(List.of(template));
    }

    public void deleteTemplateIndexes(Long userId, List<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return;
        }

        String requestId = "template-delete-" + userId + "-" + System.currentTimeMillis();
        ragPublisher.publishTemplateIndex(RagTemplateIndexRequestDTO.builder()
                .requestId(requestId)
                .userId(userId)
                .payload(RagTemplateIndexRequestDTO.Payload.builder()
                        .templates(List.of())
                        .deleteTemplateIds(templateIds)
                        .build())
                .build());
    }

    public void reindexTemplates(List<Template> templates) {
        if (templates == null || templates.isEmpty()) {
            return;
        }

        Long userId = templates.get(0).getUser().getUserId();
        String requestId = "template-index-" + userId + "-" + System.currentTimeMillis();
        String emailTone = profileRepository.findByUser_UserId(userId)
                .map(profile -> profile.getEmailTone() != null ? profile.getEmailTone().name() : null)
                .orElse(null);

        List<RagTemplateIndexRequestDTO.TemplateItem> indexItems = templates.stream()
                .map(template -> toIndexItem(template, template.getCategory(), emailTone))
                .toList();

        ragPublisher.publishTemplateIndex(RagTemplateIndexRequestDTO.builder()
                .requestId(requestId)
                .userId(userId)
                .payload(RagTemplateIndexRequestDTO.Payload.builder()
                        .templates(indexItems)
                        .deleteTemplateIds(List.of())
                        .build())
                .build());
    }

    public void reindexCategory(Category category) {
        List<Template> templates = templateRepository.findByCategory_CategoryId(category.getCategoryId());
        if (templates.isEmpty()) {
            return;
        }

        String requestId = "template-index-" + category.getCategoryId() + "-" + System.currentTimeMillis();
        String emailTone = profileRepository.findByUser_UserId(category.getUser().getUserId())
                .map(profile -> profile.getEmailTone() != null ? profile.getEmailTone().name() : null)
                .orElse(null);

        List<RagTemplateIndexRequestDTO.TemplateItem> indexItems = templates.stream()
                .map(template -> toIndexItem(template, category, emailTone))
                .toList();

        ragPublisher.publishTemplateIndex(RagTemplateIndexRequestDTO.builder()
                .requestId(requestId)
                .userId(category.getUser().getUserId())
                .payload(RagTemplateIndexRequestDTO.Payload.builder()
                        .templates(indexItems)
                        .deleteTemplateIds(List.of())
                        .build())
                .build());
    }

    private RagTemplateIndexRequestDTO.TemplateItem toIndexItem(
            Template template,
            Category category,
            String emailTone
    ) {
        String variantLabel = template.getVariantLabel() != null ? template.getVariantLabel() : "일반형";
        String canonicalText = buildCanonicalText(template, category, emailTone, variantLabel);
        template.prepareIndexing(canonicalText);

        return RagTemplateIndexRequestDTO.TemplateItem.builder()
                .templateId(template.getTemplateId())
                .title(template.getTitle())
                .categoryName(category.getCategoryName())
                .canonicalText(canonicalText)
                .emailTone(emailTone)
                .metadata(RagTemplateIndexRequestDTO.Metadata.builder()
                        .searchSummary(variantLabel + " 템플릿")
                        .semanticKeywords(toSemanticKeywords(category, variantLabel))
                        .recommendedSituations(List.of())
                        .build())
                .build();
    }

    private String buildCanonicalText(
            Template template,
            Category category,
            String emailTone,
            String variantLabel
    ) {
        return """
                제목: %s
                카테고리: %s
                유형: %s
                어조: %s
                메일 제목: %s
                본문:
                %s
                """.formatted(
                template.getTitle(),
                category.getCategoryName(),
                variantLabel,
                emailTone != null ? emailTone : "미지정",
                template.getSubjectTemplate(),
                template.getBodyTemplate()
        ).trim();
    }

    public List<String> toSemanticKeywords(Category category, String variantLabel) {
        List<String> semanticKeywords = new ArrayList<>();
        semanticKeywords.add(category.getCategoryName());
        semanticKeywords.add(variantLabel);
        semanticKeywords.addAll(resolveCategoryKeywords(category.getCategoryName()));
        return normalizeKeywords(semanticKeywords);
    }

    public List<String> resolveCategoryKeywords(String categoryName) {
        return keywordRuleRepository.findByCategoryName(categoryName)
                .map(CategoryKeywordRule::getKeywords)
                .orElse(List.of());
    }

    private List<String> normalizeKeywords(List<String> keywords) {
        return keywords.stream()
                .filter(keyword -> keyword != null && !keyword.trim().isEmpty())
                .map(String::trim)
                .distinct()
                .toList();
    }
}
