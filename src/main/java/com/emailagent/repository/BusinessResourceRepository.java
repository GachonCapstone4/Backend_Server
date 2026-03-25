package com.emailagent.repository;

import com.emailagent.domain.entity.BusinessResource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BusinessResourceRepository extends JpaRepository<BusinessResource, Long> {
    List<BusinessResource> findByUser_UserId(Long userId);
    Optional<BusinessResource> findByResourceIdAndUser_UserId(Long resourceId, Long userId);
}
