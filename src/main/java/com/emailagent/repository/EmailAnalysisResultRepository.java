package com.emailagent.repository;

import com.emailagent.domain.entity.EmailAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailAnalysisResultRepository extends JpaRepository<EmailAnalysisResult, Long> {

    Optional<EmailAnalysisResult> findByEmail_EmailId(Long emailId);

    // 카테고리가 매칭된 이메일 수 (템플릿 매칭률 계산용)
    @Query("""
            SELECT COUNT(ar) FROM EmailAnalysisResult ar
            JOIN ar.email e
            WHERE e.user.userId = :userId
              AND ar.category IS NOT NULL
              AND e.receivedAt >= :start
              AND e.receivedAt < :end
            """)
    long countMatchedByUserIdAndDateRange(@Param("userId") Long userId,
                                          @Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    // 주간 카테고리별 이메일 수 (N+1 방지: category FETCH JOIN)
    @Query("""
            SELECT ar.category.categoryName, COUNT(ar), ar.category.color
            FROM EmailAnalysisResult ar
            JOIN ar.email e
            JOIN ar.category c
            WHERE e.user.userId = :userId
              AND e.receivedAt >= :start
              AND e.receivedAt < :end
            GROUP BY ar.category.categoryId, ar.category.categoryName, ar.category.color
            ORDER BY COUNT(ar) DESC
            """)
    List<Object[]> findWeeklyCategorySummary(@Param("userId") Long userId,
                                              @Param("start") LocalDateTime start,
                                              @Param("end") LocalDateTime end);

    // 이메일 ID 목록으로 분석 결과 일괄 조회 (N+1 방지)
    @Query("""
            SELECT ar FROM EmailAnalysisResult ar
            LEFT JOIN FETCH ar.category
            WHERE ar.email.emailId IN :emailIds
            """)
    List<EmailAnalysisResult> findByEmailIdsWithCategory(@Param("emailIds") List<Long> emailIds);
}
