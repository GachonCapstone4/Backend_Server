package com.emailagent.repository;

import com.emailagent.domain.entity.EmailTemplateRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmailTemplateRecommendationRepository extends JpaRepository<EmailTemplateRecommendation, Long> {

    @Query("""
            SELECT r FROM EmailTemplateRecommendation r
            JOIN FETCH r.template t
            WHERE r.user.userId = :userId
              AND r.email.emailId = :emailId
            ORDER BY r.rankOrder ASC, r.score DESC
            """)
    List<EmailTemplateRecommendation> findByUserIdAndEmailIdOrderByRank(
            @Param("userId") Long userId,
            @Param("emailId") Long emailId
    );

    void deleteByUser_UserIdAndEmail_EmailId(Long userId, Long emailId);

    void deleteByTemplate_TemplateIdIn(List<Long> templateIds);

    Optional<EmailTemplateRecommendation> findByEmail_EmailIdAndTemplate_TemplateId(Long emailId, Long templateId);

    @Query("""
            SELECT r FROM EmailTemplateRecommendation r
            JOIN FETCH r.template t
            WHERE r.recommendationId = :recommendationId
              AND r.user.userId = :userId
              AND r.email.emailId = :emailId
            """)
    Optional<EmailTemplateRecommendation> findSelectedRecommendation(
            @Param("recommendationId") Long recommendationId,
            @Param("userId") Long userId,
            @Param("emailId") Long emailId
    );

    @Query("""
            SELECT COUNT(DISTINCT r.email.emailId)
            FROM EmailTemplateRecommendation r
            WHERE r.user.userId = :userId
              AND r.email.status = com.emailagent.domain.enums.EmailStatus.PENDING_REVIEW
              AND NOT EXISTS (
                SELECT d FROM DraftReply d
                WHERE d.user.userId = :userId
                  AND d.email.emailId = r.email.emailId
              )
            """)
    long countPendingReviewRecommendationsWithoutDraft(@Param("userId") Long userId);

    @Modifying
    @Query("DELETE FROM EmailTemplateRecommendation r WHERE r.user.userId = :userId")
    void deleteByUser_UserId(@Param("userId") Long userId);
}
