package com.emailagent.controller.admin;

import com.emailagent.dto.request.admin.AdminUserStatusUpdateRequest;
import com.emailagent.dto.response.admin.user.AdminDeleteIntegrationResponse;
import com.emailagent.dto.response.admin.user.AdminUserDetailResponse;
import com.emailagent.dto.response.admin.user.AdminUserIntegrationResponse;
import com.emailagent.dto.response.admin.user.AdminUserListResponse;
import com.emailagent.dto.response.admin.user.AdminUserStatusUpdateResponse;
import com.emailagent.service.admin.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public AdminUserListResponse getUsers(
            @RequestParam(value = "search_type", required = false) String searchType,
            @RequestParam(value = "search_keyword", required = false) String searchKeyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return adminUserService.getUsers(searchType, searchKeyword, page, size);
    }

    @GetMapping("/{user_id}")
    public AdminUserDetailResponse getUserDetail(@PathVariable("user_id") Long userId) {
        return adminUserService.getUserDetail(userId);
    }

    @GetMapping("/{user_id}/integration")
    public AdminUserIntegrationResponse getUserIntegration(@PathVariable("user_id") Long userId) {
        return adminUserService.getUserIntegration(userId);
    }

    @PatchMapping("/{user_id}/status")
    public AdminUserStatusUpdateResponse updateUserStatus(
            @PathVariable("user_id") Long userId,
            @Valid @RequestBody AdminUserStatusUpdateRequest request) {
        return adminUserService.updateUserStatus(userId, request.getIsActive());
    }

    @DeleteMapping("/{user_id}/integration")
    public AdminDeleteIntegrationResponse deleteUserIntegration(@PathVariable("user_id") Long userId) {
        return adminUserService.deleteUserIntegration(userId);
    }
}
