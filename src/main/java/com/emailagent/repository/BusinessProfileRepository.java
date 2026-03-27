package com.emailagent.repository;

import com.emailagent.domain.entity.BusinessProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BusinessProfileRepository extends JpaRepository<BusinessProfile, Long> {

    Optional<BusinessProfile> findByUser_UserId(Long userId);

    Optional<BusinessProfile> findByProfileIdAndUser_UserId(Long profileId, Long userId);

    // 관리자 - 사용자 ID 목록으로 배치 조회 (N+1 방지)
    @Query("SELECT bp FROM BusinessProfile bp WHERE bp.user.userId IN :userIds")
    List<BusinessProfile> findAllByUserIds(@Param("userIds") List<Long> userIds);
}
