package com.emailagent.repository;

import com.emailagent.domain.entity.GooglePendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;

public interface GooglePendingRegistrationRepository extends JpaRepository<GooglePendingRegistration, String> {

    // 만료된 레코드 일괄 삭제 (필요 시 스케줄러에서 호출)
    @Modifying
    @Query("DELETE FROM GooglePendingRegistration g WHERE g.expiresAt < :now")
    void deleteExpired(LocalDateTime now);
}
