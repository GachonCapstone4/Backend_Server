package com.emailagent.controller.admin;

import com.emailagent.dto.request.admin.AdminSupportTicketReplyRequest;
import com.emailagent.dto.response.admin.support.AdminSupportTicketDetailResponse;
import com.emailagent.dto.response.admin.support.AdminSupportTicketListResponse;
import com.emailagent.dto.response.admin.support.AdminSupportTicketReplyResponse;
import com.emailagent.security.CurrentUser;
import com.emailagent.service.admin.AdminSupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/support-tickets")
@RequiredArgsConstructor
public class AdminSupportTicketController {

    private final AdminSupportTicketService adminSupportTicketService;

    @GetMapping
    public AdminSupportTicketListResponse getTickets(
            @RequestParam(required = false) String status,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminSupportTicketService.getTickets(status, userId, page, size);
    }

    @GetMapping("/{ticket_id}")
    public AdminSupportTicketDetailResponse getTicketDetail(@PathVariable("ticket_id") Long ticketId) {
        return adminSupportTicketService.getTicketDetail(ticketId);
    }

    @PostMapping("/{ticket_id}/reply")
    public AdminSupportTicketReplyResponse replyToTicket(
            @PathVariable("ticket_id") Long ticketId,
            @Valid @RequestBody AdminSupportTicketReplyRequest request,
            @CurrentUser Long adminUserId) {
        return adminSupportTicketService.replyToTicket(ticketId, request.getAdminReply(), adminUserId);
    }
}
