package com.venue.mgmt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HeatTaggingRules {
    @JsonProperty("heat_tags")
    private List<HeatTag> heatTags;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeatTag {
        @JsonProperty("score_range")
        private ScoreRange scoreRange;

        private String tag;
        private String meaning;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ScoreRange {
        @JsonProperty("greaterThanOrEqual")
        private int greaterThanOrEqual;

        @JsonProperty("lessThanOrEqual")
        private int lessThanOrEqual;
    }
}
