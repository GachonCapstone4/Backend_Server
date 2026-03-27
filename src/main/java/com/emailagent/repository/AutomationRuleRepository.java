package com.emailagent.repository;

import com.emailagent.domain.entity.AutomationRule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, Long> {

    // N+1 방지: category, template FETCH JOIN
    @Query("""
            SELECT ar FROM AutomationRule ar
            JOIN FETCH ar.category
            LEFT JOIN FETCH ar.template
            WHERE ar.user.userId = :userId
            ORDER BY ar.createdAt DESC
            """)
    List<AutomationRule> findByUserIdWithDetails(@Param("userId") Long userId);

    Optional<AutomationRule> findByRuleIdAndUser_UserId(Long ruleId, Long userId);

    // 관리자 - 전체 목록 (native, GROUP_CONCAT으로 keywords N+1 방지, 페이징)
    @Query(value = """
            SELECT ar.rule_id, ar.user_id,
                   COALESCE(GROUP_CONCAT(ark.keyword ORDER BY ark.keyword SEPARATOR ', '), '') AS keywords,
                   ar.is_active
            FROM AutomationRules ar
            LEFT JOIN AutomationRuleKeywords ark ON ar.rule_id = ark.rule_id
            GROUP BY ar.rule_id, ar.user_id, ar.is_active, ar.created_at
            ORDER BY ar.created_at DESC
            """,
            countQuery = "SELECT COUNT(*) FROM AutomationRules",
            nativeQuery = true)
    Page<Object[]> findAllWithKeywords(Pageable pageable);

    // 관리자 - 특정 사용자 필터 (native)
    @Query(value = """
            SELECT ar.rule_id, ar.user_id,
                   COALESCE(GROUP_CONCAT(ark.keyword ORDER BY ark.keyword SEPARATOR ', '), '') AS keywords,
                   ar.is_active
            FROM AutomationRules ar
            LEFT JOIN AutomationRuleKeywords ark ON ar.rule_id = ark.rule_id
            WHERE ar.user_id = :userId
            GROUP BY ar.rule_id, ar.user_id, ar.is_active, ar.created_at
            ORDER BY ar.created_at DESC
            """,
            countQuery = "SELECT COUNT(*) FROM AutomationRules WHERE user_id = :userId",
            nativeQuery = true)
    Page<Object[]> findByUserIdWithKeywords(@Param("userId") Long userId, Pageable pageable);

    // 관리자 - 상세 조회: keywords FETCH JOIN
    @Query("""
            SELECT ar FROM AutomationRule ar
            JOIN FETCH ar.category
            LEFT JOIN FETCH ar.template
            WHERE ar.ruleId = :ruleId
            """)
    Optional<AutomationRule> findDetailByRuleId(@Param("ruleId") Long ruleId);
}
