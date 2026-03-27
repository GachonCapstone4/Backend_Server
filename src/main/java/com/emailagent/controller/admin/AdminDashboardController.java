package com.emailagent.controller.admin;

import com.emailagent.dto.response.admin.dashboard.AdminDashboardSummaryResponse;
import com.emailagent.dto.response.admin.dashboard.AdminDomainDistributionResponse;
import com.emailagent.dto.response.admin.dashboard.AdminEmailVolumeResponse;
import com.emailagent.dto.response.admin.dashboard.AdminWeeklyTrendResponse;
import com.emailagent.service.admin.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public AdminDashboardSummaryResponse getSummary() {
        return adminDashboardService.getSummary();
    }

    @GetMapping("/email-volume")
    public AdminEmailVolumeResponse getEmailVolume(
            @RequestParam("start_date") String startDate,
            @RequestParam("end_date") String endDate) {
        return adminDashboardService.getEmailVolume(startDate, endDate);
    }

    @GetMapping("/domain-distribution")
    public AdminDomainDistributionResponse getDomainDistribution(
            @RequestParam Integer limit) {
        return adminDashboardService.getDomainDistribution(limit);
    }

    @GetMapping("/weekly-trend")
    public AdminWeeklyTrendResponse getWeeklyTrend() {
        return adminDashboardService.getWeeklyTrend();
    }
}
