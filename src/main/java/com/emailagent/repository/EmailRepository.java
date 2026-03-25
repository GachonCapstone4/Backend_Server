package com.emailagent.repository;

import com.emailagent.domain.entity.Email;
import com.emailagent.domain.enums.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {

    // 사용자의 이메일 목록 (페이징)
    Page<Email> findByUser_UserIdOrderByReceivedAtDesc(Long userId, Pageable pageable);

    // 상태별 조회
    Page<Email> findByUser_UserIdAndStatusOrderByReceivedAtDesc(
            Long userId, EmailStatus status, Pageable pageable);

    // Gmail 메시지 ID로 중복 체크
    boolean existsByExternalMsgId(String externalMsgId);

    Optional<Email> findByExternalMsgId(String externalMsgId);

    // 분석 결과 포함 조회 (N+1 방지)
    @Query("SELECT e FROM Email e " +
           "LEFT JOIN FETCH e.user " +
           "WHERE e.emailId = :emailId AND e.user.userId = :userId")
    Optional<Email> findByEmailIdAndUserId(@Param("emailId") Long emailId,
                                            @Param("userId") Long userId);

    // 특정 기간 이메일 수 카운트 (대시보드용)
    @Query("SELECT COUNT(e) FROM Email e WHERE e.user.userId = :userId AND e.receivedAt >= :start AND e.receivedAt < :end")
    long countByUserIdAndDateRange(@Param("userId") Long userId,
                                   @Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    // 최근 이메일 N건 (대시보드용, N+1 방지)
    @Query("SELECT e FROM Email e WHERE e.user.userId = :userId ORDER BY e.receivedAt DESC")
    List<Email> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    // 수신함 목록 — 분석결과+카테고리 FETCH JOIN, 별도 countQuery로 페이징 정확성 보장
    @Query(value = """
            SELECT e FROM Email e
            LEFT JOIN FETCH e.analysisResult ar
            LEFT JOIN FETCH ar.category
            WHERE e.user.userId = :userId
            ORDER BY e.receivedAt DESC
            """,
           countQuery = "SELECT COUNT(e) FROM Email e WHERE e.user.userId = :userId")
    Page<Email> findInboxPage(@Param("userId") Long userId, Pageable pageable);

    @Query(value = """
            SELECT e FROM Email e
            LEFT JOIN FETCH e.analysisResult ar
            LEFT JOIN FETCH ar.category
            WHERE e.user.userId = :userId AND e.status = :status
            ORDER BY e.receivedAt DESC
            """,
           countQuery = "SELECT COUNT(e) FROM Email e WHERE e.user.userId = :userId AND e.status = :status")
    Page<Email> findInboxPageByStatus(@Param("userId") Long userId,
                                      @Param("status") EmailStatus status,
                                      Pageable pageable);

    // 상세 조회 — 분석결과+카테고리 FETCH JOIN
    @Query("""
            SELECT e FROM Email e
            LEFT JOIN FETCH e.analysisResult ar
            LEFT JOIN FETCH ar.category
            WHERE e.emailId = :emailId AND e.user.userId = :userId
            """)
    Optional<Email> findDetailByEmailIdAndUserId(@Param("emailId") Long emailId,
                                                  @Param("userId") Long userId);
}
