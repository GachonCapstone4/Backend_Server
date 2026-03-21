package com.emailagent.dto.response;

import com.emailagent.domain.entity.BusinessResource;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class BusinessResourceResponse {
    private Long resourceId;
    private String title;
    private String fileName;
    private String fileType;
    private LocalDateTime createdAt;

    public static BusinessResourceResponse from(BusinessResource resource) {
        return BusinessResourceResponse.builder()
                .resourceId(resource.getResourceId())
                .title(resource.getTitle())
                .fileName(resource.getFileName())
                .fileType(resource.getFileType())
                .createdAt(resource.getCreatedAt())
                .build();
    }
}
