package com.emailagent.service;

import com.emailagent.domain.converter.DisplayConfigConverter;
import com.emailagent.domain.entity.User;
import com.emailagent.domain.value.UserDisplayConfig;
import com.emailagent.dto.request.auth.DisplaySettingsRequest;
import com.emailagent.dto.request.auth.PasswordChangeRequest;
import com.emailagent.dto.request.auth.UserProfileUpdateRequest;
import com.emailagent.dto.response.auth.BaseResponse;
import com.emailagent.dto.response.auth.DisplaySettingsResponse;
import com.emailagent.dto.response.auth.EmailAvailabilityResponse;
import com.emailagent.dto.response.auth.UserProfileResponse;
import com.emailagent.dto.response.auth.UserUpdateResponse;
import com.emailagent.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDataCleanupService userDataCleanupService;
    private final GoogleOAuthService googleOAuthService;

    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(Long userId) {
        User user = findActiveUser(userId);
        return new UserProfileResponse(user);
    }

    @Transactional
    public UserUpdateResponse updateMyProfile(Long userId, UserProfileUpdateRequest request) {
        User user = findActiveUser(userId);
        user.updateName(request.getName());
        return new UserUpdateResponse(user);
    }

    @Transactional
    public BaseResponse deleteMe(Long userId) {
        User user = findActiveUser(userId);
        // Gmail watch 중단 후 Google 토큰 revoke — DB 정리 전에 수행
        googleOAuthService.stopGmailWatchIfPresent(userId);
        googleOAuthService.revokeTokenIfPresent(userId);
        userDataCleanupService.clearAllUserData(userId);
        userRepository.delete(user);
        return new BaseResponse();
    }

    @Transactional(readOnly = true)
    public EmailAvailabilityResponse checkEmailAvailability(String email) {
        boolean available = !userRepository.existsByEmail(email);
        return new EmailAvailabilityResponse(available);
    }

    @Transactional
    public BaseResponse changePassword(Long userId, PasswordChangeRequest request) {
        User user = findActiveUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BadCredentialsException("현재 비밀번호가 올바르지 않습니다.");
        }
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        return new BaseResponse();
    }

    @Transactional(readOnly = true)
    public DisplaySettingsResponse getDisplaySettings(Long userId) {
        User user = findActiveUser(userId);
        UserDisplayConfig configs = DisplayConfigConverter.normalize(user.getDisplayConfigs());
        return new DisplaySettingsResponse(configs);
    }

    @Transactional
    public DisplaySettingsResponse updateDisplaySettings(Long userId, DisplaySettingsRequest request) {
        User user = findActiveUser(userId);
        UserDisplayConfig configs = DisplayConfigConverter.normalize(request.toConfig());
        user.updateDisplayConfigs(configs);
        return new DisplaySettingsResponse(configs);
    }

    private User findActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        if (!user.isActive()) {
            throw new IllegalStateException("비활성화된 계정입니다.");
        }
        return user;
    }
}
