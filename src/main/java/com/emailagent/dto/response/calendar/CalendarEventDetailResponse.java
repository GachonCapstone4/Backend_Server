package com.emailagent.dto.response.calendar;

import com.emailagent.domain.entity.CalendarEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalendarEventDetailResponse {

    private Long eventId;
    private String title;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String source;
    private String status;
    private boolean isCalendarAdded;
    private Long emailId;       // 이메일에서 감지된 경우 연결된 이메일 ID (nullable)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CalendarEventDetailResponse from(CalendarEvent event) {
        return CalendarEventDetailResponse.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .startDatetime(event.getStartDatetime())
                .endDatetime(event.getEndDatetime())
                .source(event.getSource())
                .status(event.getStatus())
                .isCalendarAdded(event.isCalendarAdded())
                .emailId(event.getEmail() != null ? event.getEmail().getEmailId() : null)
                .createdAt(event.getCreatedAt())
                .updatedAt(event.getUpdatedAt())
                .build();
    }
}
