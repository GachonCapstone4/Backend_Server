package com.emailagent.controller.admin;

import com.emailagent.dto.request.admin.AdminAutomationRuleCreateRequest;
import com.emailagent.dto.request.admin.AdminAutomationRuleUpdateRequest;
import com.emailagent.dto.response.admin.AdminSimpleResponse;
import com.emailagent.dto.response.admin.automation.AdminAutomationRuleCreateResponse;
import com.emailagent.dto.response.admin.automation.AdminAutomationRuleDetailResponse;
import com.emailagent.dto.response.admin.automation.AdminAutomationRuleListResponse;
import com.emailagent.dto.response.admin.automation.AdminAutomationRuleUpdateResponse;
import com.emailagent.service.admin.AdminAutomationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/automations/rules")
@RequiredArgsConstructor
public class AdminAutomationController {

    private final AdminAutomationService adminAutomationService;

    @GetMapping
    public AdminAutomationRuleListResponse getRules(
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminAutomationService.getRules(userId, page, size);
    }

    @GetMapping("/{rule_id}")
    public AdminAutomationRuleDetailResponse getRuleDetail(@PathVariable("rule_id") Long ruleId) {
        return adminAutomationService.getRuleDetail(ruleId);
    }

    @PostMapping
    public AdminAutomationRuleCreateResponse createRule(
            @Valid @RequestBody AdminAutomationRuleCreateRequest request) {
        return adminAutomationService.createRule(request);
    }

    @PatchMapping("/{rule_id}")
    public AdminAutomationRuleUpdateResponse updateRule(
            @PathVariable("rule_id") Long ruleId,
            @RequestBody AdminAutomationRuleUpdateRequest request) {
        return adminAutomationService.updateRule(ruleId, request);
    }

    @DeleteMapping("/{rule_id}")
    public AdminSimpleResponse deleteRule(@PathVariable("rule_id") Long ruleId) {
        return adminAutomationService.deleteRule(ruleId);
    }
}
