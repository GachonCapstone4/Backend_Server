package com.emailagent.repository;

import com.emailagent.domain.entity.BusinessFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BusinessFaqRepository extends JpaRepository<BusinessFaq, Long> {
    List<BusinessFaq> findByUser_UserId(Long userId);
    Optional<BusinessFaq> findByFaqIdAndUser_UserId(Long faqId, Long userId);

    @Modifying
    @Query("DELETE FROM BusinessFaq f WHERE f.user.userId = :userId")
    void deleteByUser_UserId(@Param("userId") Long userId);
}
