package com.emailagent.repository;

import com.emailagent.domain.entity.BusinessResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BusinessResourceRepository extends JpaRepository<BusinessResource, Long> {
    List<BusinessResource> findByUser_UserId(Long userId);
    Optional<BusinessResource> findByResourceIdAndUser_UserId(Long resourceId, Long userId);

    @Modifying
    @Query("DELETE FROM BusinessResource r WHERE r.user.userId = :userId")
    void deleteByUser_UserId(@Param("userId") Long userId);
}
