package com.emailagent.controller.admin;

import com.emailagent.dto.response.admin.operation.AdminJobDeleteResponse;
import com.emailagent.dto.response.admin.operation.AdminJobDetailResponse;
import com.emailagent.dto.response.admin.operation.AdminJobErrorResponse;
import com.emailagent.dto.response.admin.operation.AdminJobListResponse;
import com.emailagent.dto.response.admin.operation.AdminJobSummaryResponse;
import com.emailagent.service.admin.AdminOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/operations/jobs")
@RequiredArgsConstructor
public class AdminOperationController {

    private final AdminOperationService adminOperationService;

    @GetMapping
    public AdminJobListResponse getJobs(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminOperationService.getJobs(status, page, size);
    }

    @GetMapping("/summary")
    public AdminJobSummaryResponse getJobsSummary() {
        return adminOperationService.getJobsSummary();
    }

    @GetMapping("/{job_id}")
    public AdminJobDetailResponse getJobDetail(@PathVariable("job_id") Long jobId) {
        return adminOperationService.getJobDetail(jobId);
    }

    @GetMapping("/{job_id}/error")
    public AdminJobErrorResponse getJobError(@PathVariable("job_id") Long jobId) {
        return adminOperationService.getJobError(jobId);
    }

    @DeleteMapping("/{job_id}")
    public AdminJobDeleteResponse deleteJob(@PathVariable("job_id") Long jobId) {
        return adminOperationService.deleteJob(jobId);
    }
}
