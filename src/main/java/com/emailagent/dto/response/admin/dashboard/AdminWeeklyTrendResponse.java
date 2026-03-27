package com.emailagent.dto.response.admin.dashboard;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminWeeklyTrendResponse extends BaseResponse {

    @JsonProperty("trend_data")
    private final List<TrendEntry> trendData;

    public AdminWeeklyTrendResponse(List<TrendEntry> trendData) {
        this.trendData = trendData;
    }

    @Getter
    public static class TrendEntry {
        private final String date;

        @JsonProperty("received_count")
        private final long receivedCount;

        @JsonProperty("draft_count")
        private final long draftCount;

        public TrendEntry(String date, long receivedCount, long draftCount) {
            this.date = date;
            this.receivedCount = receivedCount;
            this.draftCount = draftCount;
        }
    }
}
