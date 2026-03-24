package com.emailagent.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class PasswordChangeRequest {

    @NotBlank(message = "현재 비밀번호는 필수입니다.")
    @JsonProperty("old_password")
    private String oldPassword;

    @NotBlank
    @Size(min = 8, message = "새 비밀번호는 8자 이상이어야 합니다.")
    @JsonProperty("new_password")
    private String newPassword;
}
