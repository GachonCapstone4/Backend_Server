package com.emailagent.dto.response.admin.dashboard;

import com.emailagent.dto.response.auth.BaseResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminDomainDistributionResponse extends BaseResponse {

    @JsonProperty("domain_data")
    private final List<DomainEntry> domainData;

    public AdminDomainDistributionResponse(List<DomainEntry> domainData) {
        this.domainData = domainData;
    }

    @Getter
    public static class DomainEntry {
        private final String domain;
        private final long count;

        public DomainEntry(String domain, long count) {
            this.domain = domain;
            this.count = count;
        }
    }
}
