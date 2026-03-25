package com.emailagent.controller;

import com.emailagent.dto.response.dashboard.*;
import com.emailagent.security.CurrentUser;
import com.emailagent.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary(@CurrentUser Long userId) {
        return ResponseEntity.ok(dashboardService.getSummary(userId));
    }

    @GetMapping("/schedules")
    public ResponseEntity<ScheduleResponse> getSchedules(@CurrentUser Long userId) {
        return ResponseEntity.ok(dashboardService.getSchedules(userId));
    }

    @GetMapping("/weekly-summary")
    public ResponseEntity<WeeklySummaryResponse> getWeeklySummary(@CurrentUser Long userId) {
        return ResponseEntity.ok(dashboardService.getWeeklySummary(userId));
    }

    @GetMapping("/recent-emails")
    public ResponseEntity<RecentEmailResponse> getRecentEmails(@CurrentUser Long userId) {
        return ResponseEntity.ok(dashboardService.getRecentEmails(userId));
    }
}
