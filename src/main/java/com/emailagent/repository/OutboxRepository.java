package com.emailagent.repository;

import com.emailagent.domain.entity.Outbox;
import com.emailagent.domain.enums.OutboxStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    List<Outbox> findByStatusAndRetryCountLessThan(OutboxStatus status, int maxRetry);

    // 타임아웃 감지: SENDING 상태인데 일정 시간 이상 경과
    @Query("SELECT o FROM Outbox o WHERE o.status = 'SENDING' AND o.sentAt < :timeout")
    List<Outbox> findTimedOutMessages(LocalDateTime timeout);

    // 관리자 - 전체 작업 목록 페이징 (email FETCH JOIN으로 N+1 방지)
    @Query(value = "SELECT o FROM Outbox o JOIN FETCH o.email ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Outbox o")
    Page<Outbox> findAllWithEmailOrderByCreatedAtDesc(Pageable pageable);

    // 관리자 - 상태 필터 페이징 (email FETCH JOIN)
    @Query(value = "SELECT o FROM Outbox o JOIN FETCH o.email WHERE o.status = :status ORDER BY o.createdAt DESC",
           countQuery = "SELECT COUNT(o) FROM Outbox o WHERE o.status = :status")
    Page<Outbox> findByStatusWithEmailOrderByCreatedAtDesc(@Param("status") OutboxStatus status, Pageable pageable);

    // 관리자 - 상태별 건수
    long countByStatus(OutboxStatus status);
}
