package com.emailagent.repository;

import com.emailagent.domain.entity.SupportTicket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    // 전체 조회 (최신순)
    List<SupportTicket> findByUser_UserIdOrderByCreatedAtDesc(Long userId);

    // status 필터 조회 (최신순)
    List<SupportTicket> findByUser_UserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    // 소유권 검증 포함 단건 조회
    Optional<SupportTicket> findByTicketIdAndUser_UserId(Long ticketId, Long userId);

    // 관리자 - 사용자 문의 건수
    long countByUser_UserId(Long userId);

    // 관리자 - 전체 목록 페이징 (user FETCH JOIN으로 N+1 방지)
    @Query(value = "SELECT t FROM SupportTicket t JOIN FETCH t.user ORDER BY t.createdAt DESC",
           countQuery = "SELECT COUNT(t) FROM SupportTicket t")
    Page<SupportTicket> findAllWithUserOrderByCreatedAtDesc(Pageable pageable);

    // 관리자 - status 필터 페이징 (user FETCH JOIN)
    @Query(value = "SELECT t FROM SupportTicket t JOIN FETCH t.user WHERE t.status = :status ORDER BY t.createdAt DESC",
           countQuery = "SELECT COUNT(t) FROM SupportTicket t WHERE t.status = :status")
    Page<SupportTicket> findByStatusWithUserOrderByCreatedAtDesc(@Param("status") String status, Pageable pageable);

    // 관리자 - 특정 사용자 문의 페이징 (user FETCH JOIN)
    @Query(value = "SELECT t FROM SupportTicket t JOIN FETCH t.user WHERE t.user.userId = :userId ORDER BY t.createdAt DESC",
           countQuery = "SELECT COUNT(t) FROM SupportTicket t WHERE t.user.userId = :userId")
    Page<SupportTicket> findByUserIdWithUserOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    // 관리자 - 특정 사용자 + status 필터 페이징 (user FETCH JOIN)
    @Query(value = "SELECT t FROM SupportTicket t JOIN FETCH t.user WHERE t.user.userId = :userId AND t.status = :status ORDER BY t.createdAt DESC",
           countQuery = "SELECT COUNT(t) FROM SupportTicket t WHERE t.user.userId = :userId AND t.status = :status")
    Page<SupportTicket> findByUserIdAndStatusWithUserOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("status") String status, Pageable pageable);
}
