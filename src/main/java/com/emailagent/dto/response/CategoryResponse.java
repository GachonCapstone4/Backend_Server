package com.emailagent.dto.response;

import com.emailagent.domain.entity.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    private Long categoryId;
    private String categoryName;
    private String color;

    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .categoryId(category.getCategoryId())
                .categoryName(category.getCategoryName())
                .color(category.getColor())
                .build();
    }
}
