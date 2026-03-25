package com.emailagent.repository;

import com.emailagent.domain.entity.DraftReply;
import com.emailagent.domain.enums.DraftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface DraftReplyRepository extends JpaRepository<DraftReply, Long> {

    long countByUser_UserIdAndStatus(Long userId, DraftStatus status);

    // template FETCH JOIN (N+1 방지)
    @Query("""
            SELECT d FROM DraftReply d
            LEFT JOIN FETCH d.template
            WHERE d.email.emailId = :emailId AND d.user.userId = :userId
            """)
    Optional<DraftReply> findByEmailIdAndUserId(@Param("emailId") Long emailId,
                                                 @Param("userId") Long userId);
}
