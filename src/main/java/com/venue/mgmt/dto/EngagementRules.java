package com.venue.mgmt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngagementRules {
    private List<EngagementRule> engagement;
    private String dimension;

    @JsonProperty("max_score")
    private int maxScore;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EngagementRule {
        private List<String> action;
        private int score;
    }
}
