package com.emailagent.repository;

import com.emailagent.domain.entity.AutomationRule;
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
}
