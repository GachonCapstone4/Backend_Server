package com.emailagent.dto.response.calendar;

import com.emailagent.domain.entity.CalendarEvent;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CalendarEventResponse {

    private Long eventId;
    private String title;
    private LocalDateTime startDatetime;
    private LocalDateTime endDatetime;
    private String source;
    private String status;
    private boolean isCalendarAdded;
    private LocalDateTime createdAt;

    public static CalendarEventResponse from(CalendarEvent event) {
        return CalendarEventResponse.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .startDatetime(event.getStartDatetime())
                .endDatetime(event.getEndDatetime())
                .source(event.getSource())
                .status(event.getStatus())
                .isCalendarAdded(event.isCalendarAdded())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
