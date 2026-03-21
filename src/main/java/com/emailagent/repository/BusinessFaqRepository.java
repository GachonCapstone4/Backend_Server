package com.emailagent.repository;

import com.emailagent.domain.entity.BusinessFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BusinessFaqRepository extends JpaRepository<BusinessFaq, Long> {
    List<BusinessFaq> findByUser_UserId(Long userId);
    Optional<BusinessFaq> findByFaqIdAndUser_UserId(Long faqId, Long userId);
}
