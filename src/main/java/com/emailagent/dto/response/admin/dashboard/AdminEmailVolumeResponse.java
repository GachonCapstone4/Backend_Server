package com.emailagent.dto.response.admin.dashboard;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminEmailVolumeResponse extends BaseResponse {

    @JsonProperty("volume_data")
    private final List<VolumeEntry> volumeData;

    public AdminEmailVolumeResponse(List<VolumeEntry> volumeData) {
        this.volumeData = volumeData;
    }

    @Getter
    public static class VolumeEntry {
        private final String date;
        private final long count;

        public VolumeEntry(String date, long count) {
            this.date = date;
            this.count = count;
        }
    }
}
