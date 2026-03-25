package com.emailagent.controller;

import com.emailagent.dto.request.support.SupportTicketRequest;
import com.emailagent.dto.response.support.SupportTicketDetailResponse;
import com.emailagent.dto.response.support.SupportTicketListResponse;
import com.emailagent.security.CurrentUser;
import com.emailagent.service.SupportTicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support-tickets")
@RequiredArgsConstructor
public class SupportTicketController {

    private final SupportTicketService supportTicketService;

    // GET /api/support-tickets?status=PENDING|ANSWERED
    @GetMapping
    public ResponseEntity<SupportTicketListResponse> getTickets(
            @CurrentUser Long userId,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(supportTicketService.getTickets(userId, status));
    }

    // GET /api/support-tickets/{ticket_id}
    @GetMapping("/{ticketId}")
    public ResponseEntity<SupportTicketDetailResponse> getTicket(
            @CurrentUser Long userId,
            @PathVariable Long ticketId) {
        return ResponseEntity.ok(supportTicketService.getTicket(userId, ticketId));
    }

    // POST /api/support-tickets
    @PostMapping
    public ResponseEntity<SupportTicketDetailResponse.CreateResponse> createTicket(
            @CurrentUser Long userId,
            @Valid @RequestBody SupportTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportTicketService.createTicket(userId, request));
    }
}
