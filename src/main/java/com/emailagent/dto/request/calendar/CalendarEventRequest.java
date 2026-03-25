package com.emailagent.dto.request.calendar;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CalendarEventRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotNull(message = "시작 일시는 필수입니다.")
    private LocalDateTime startDatetime;

    private LocalDateTime endDatetime;
}
