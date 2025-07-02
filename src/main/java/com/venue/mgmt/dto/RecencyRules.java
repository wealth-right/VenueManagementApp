package com.venue.mgmt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RecencyRules {
    private List<RecencyRule> recency;
    private String dimension;

    @JsonProperty("recencyFrequency")
    private String recencyFrequency;

    @JsonProperty("max_score")
    private int maxScore;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecencyRule {
        @JsonProperty("time_period")
        private TimePeriod timePeriod;

        private String type;
        private int score;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimePeriod {
        @JsonProperty("lessThan")
        private Integer lessThan;

        @JsonProperty("greaterThan")
        private Integer greaterThan;
    }
}
