package com.emailagent.service;

import com.emailagent.domain.entity.SupportTicket;
import com.emailagent.domain.entity.User;
import com.emailagent.dto.request.support.SupportTicketRequest;
import com.emailagent.dto.response.support.SupportTicketDetailResponse;
import com.emailagent.dto.response.support.SupportTicketListResponse;
import com.emailagent.exception.ResourceNotFoundException;
import com.emailagent.repository.SupportTicketRepository;
import com.emailagent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;

    // =============================================
    // GET /api/support-tickets?status=
    // status 없으면 전체, 있으면 필터 조회
    // =============================================
    @Transactional(readOnly = true)
    public SupportTicketListResponse getTickets(Long userId, String status) {
        List<SupportTicket> tickets;

        if (status != null && !status.isBlank()) {
            tickets = supportTicketRepository
                    .findByUser_UserIdAndStatusOrderByCreatedAtDesc(userId, status.toUpperCase());
        } else {
            tickets = supportTicketRepository
                    .findByUser_UserIdOrderByCreatedAtDesc(userId);
        }

        return SupportTicketListResponse.of(tickets);
    }

    // =============================================
    // GET /api/support-tickets/{ticket_id}
    // 본인 티켓인지 userId 검증 포함
    // =============================================
    @Transactional(readOnly = true)
    public SupportTicketDetailResponse getTicket(Long userId, Long ticketId) {
        SupportTicket ticket = findTicketForUser(ticketId, userId);
        return SupportTicketDetailResponse.from(ticket);
    }

    // =============================================
    // POST /api/support-tickets
    // status=PENDING, admin_reply=null 으로 저장
    // =============================================
    @Transactional
    public SupportTicketDetailResponse.CreateResponse createTicket(Long userId, SupportTicketRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다."));

        SupportTicket ticket = SupportTicket.builder()
                .user(user)
                .title(request.getTitle())
                .content(request.getContent())
                .build(); // status 기본값 PENDING, adminReply null

        SupportTicket saved = supportTicketRepository.save(ticket);

        return SupportTicketDetailResponse.CreateResponse.builder()
                .ticketId(saved.getTicketId())
                .build();
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────────────────

    private SupportTicket findTicketForUser(Long ticketId, Long userId) {
        return supportTicketRepository
                .findByTicketIdAndUser_UserId(ticketId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("문의를 찾을 수 없습니다."));
    }
}
