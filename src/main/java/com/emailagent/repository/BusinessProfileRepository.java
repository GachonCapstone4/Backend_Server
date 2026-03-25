package com.emailagent.repository;

import com.emailagent.domain.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {
    Optional<BusinessProfile> findByUser_UserId(Long userId);
    Optional<BusinessProfile> findByProfileIdAndUser_UserId(Long profileId, Long userId);
}
