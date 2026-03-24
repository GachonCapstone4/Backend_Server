package com.emailagent.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserProfileUpdateRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name;
}
