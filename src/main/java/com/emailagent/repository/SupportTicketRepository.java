package com.emailagent.repository;

import com.emailagent.domain.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    // 전체 조회 (최신순)
    List<SupportTicket> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // status 필터 조회 (최신순)
    List<SupportTicket> findByUser_UserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    // 소유권 검증 포함 단건 조회
    Optional<SupportTicket> findByTicketIdAndUser_UserId(Long ticketId, Long userId);
}
