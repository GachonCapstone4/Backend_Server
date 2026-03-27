package com.emailagent.service.admin;

import com.emailagent.domain.entity.Template;
import com.emailagent.dto.response.admin.template.AdminTemplateCategoryStatResponse;
import com.emailagent.dto.response.admin.template.AdminTemplateListResponse;
import com.emailagent.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminTemplateService {

    private final TemplateRepository templateRepository;

    /**
     * 전체 템플릿 목록 조회 (user_id 선택 필터, 페이징)
     */
    public AdminTemplateListResponse getTemplates(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page > 0 ? page - 1 : 0, size);

        Page<Template> templatePage = (userId != null)
                ? templateRepository.findByUserIdWithUserOrderByCreatedAtDesc(userId, pageable)
                : templateRepository.findAllWithUserOrderByCreatedAtDesc(pageable);

        List<AdminTemplateListResponse.TemplateItem> items = templatePage.getContent().stream()
                .map(t -> new AdminTemplateListResponse.TemplateItem(
                        t.getTemplateId(),
                        t.getUser().getUserId(),
                        t.getTitle(),
                        t.getCreatedAt().toInstant(ZoneOffset.UTC).toString()
                ))
                .collect(Collectors.toList());

        return new AdminTemplateListResponse(templatePage.getTotalElements(), items);
    }

    /**
     * 카테고리별 템플릿 수 및 누적 사용 횟수 통계 조회
     * - TemplateUsageLogs 테이블과 Native JOIN 집계
     */
    public AdminTemplateCategoryStatResponse getCategoryStatistics() {
        List<Object[]> rows = templateRepository.findCategoryStatistics();

        List<AdminTemplateCategoryStatResponse.CategoryStat> stats = rows.stream()
                .map(row -> new AdminTemplateCategoryStatResponse.CategoryStat(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());

        return new AdminTemplateCategoryStatResponse(stats);
    }
}
