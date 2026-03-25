package com.emailagent.controller;

import com.emailagent.dto.request.automation.AutomationRuleRequest;
import com.emailagent.dto.request.automation.AutomationToggleRequest;
import com.emailagent.dto.response.automation.AutomationRuleResponse;
import com.emailagent.security.CurrentUser;
import com.emailagent.service.AutomationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/automations")
@RequiredArgsConstructor
public class AutomationController {

    private final AutomationService automationService;

    // GET /api/automations/rules
    @GetMapping("/rules")
    public ResponseEntity<AutomationRuleResponse.ListResponse> getRules(@CurrentUser Long userId) {
        return ResponseEntity.ok(automationService.getRules(userId));
    }

    // POST /api/automations/rules
    @PostMapping("/rules")
    public ResponseEntity<AutomationRuleResponse> createRule(
            @CurrentUser Long userId,
            @Valid @RequestBody AutomationRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(automationService.createRule(userId, request));
    }

    // PUT /api/automations/rules/{rule_id}
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<AutomationRuleResponse> updateRule(
            @CurrentUser Long userId,
            @PathVariable Long ruleId,
            @Valid @RequestBody AutomationRuleRequest request) {
        return ResponseEntity.ok(automationService.updateRule(userId, ruleId, request));
    }

    // DELETE /api/automations/rules/{rule_id}
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<Void> deleteRule(
            @CurrentUser Long userId,
            @PathVariable Long ruleId) {
        automationService.deleteRule(userId, ruleId);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/automations/rules/{rule_id}/auto-send
    @PatchMapping("/rules/{ruleId}/auto-send")
    public ResponseEntity<AutomationRuleResponse> toggleAutoSend(
            @CurrentUser Long userId,
            @PathVariable Long ruleId,
            @RequestBody AutomationToggleRequest request) {
        return ResponseEntity.ok(automationService.toggleAutoSend(userId, ruleId, request));
    }

    // PATCH /api/automations/rules/{rule_id}/auto-calendar
    @PatchMapping("/rules/{ruleId}/auto-calendar")
    public ResponseEntity<AutomationRuleResponse> toggleAutoCalendar(
            @CurrentUser Long userId,
            @PathVariable Long ruleId,
            @RequestBody AutomationToggleRequest request) {
        return ResponseEntity.ok(automationService.toggleAutoCalendar(userId, ruleId, request));
    }
}
