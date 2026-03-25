package com.emailagent.dto.response.dashboard;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WeeklySummaryResponse {

    private boolean success;
    private WeeklyData data;

    @Getter
    @Builder
    public static class WeeklyData {

        @JsonProperty("date_range")
        private DateRange dateRange;

        private List<CategoryStat> categories;
    }

    @Getter
    @Builder
    public static class DateRange {
        private String start; // yyyy-MM-dd
        private String end;   // yyyy-MM-dd
    }

    @Getter
    @Builder
    public static class CategoryStat {

        @JsonProperty("category_name")
        private String categoryName;

        private long count;
        private String color;
    }
}
