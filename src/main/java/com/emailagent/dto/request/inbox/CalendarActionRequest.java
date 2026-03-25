package com.emailagent.dto.request.inbox;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CalendarActionRequest {

    @NotBlank
    private String action; // ADD | IGNORE

    @JsonProperty("event_details")
    private EventDetails eventDetails; // ADD일 때만 사용

    @Getter
    public static class EventDetails {
        private String title;

        @JsonProperty("start_datetime")
        private LocalDateTime startDatetime;

        @JsonProperty("end_datetime")
        private LocalDateTime endDatetime;
    }
}
