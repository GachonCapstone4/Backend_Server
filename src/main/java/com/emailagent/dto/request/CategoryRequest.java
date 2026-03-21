package com.emailagent.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CategoryRequest {
    @NotBlank
    private String categoryName;

    private String color;
}
