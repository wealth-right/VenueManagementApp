package com.venue.mgmt.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DemographicRules {
    private String dimension;
    private int maxScore;
    private List<DemographicRule> demographic;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DemographicRule {
        private String attribute;
        private Object criteria;
        private String unit;
        private String note;
        private int score;

        // Convenience methods to access criteria as needed
        public Criteria getCriteriaObject(ObjectMapper mapper) {
            if (criteria instanceof Map<?, ?>) {
                return mapper.convertValue(criteria, Criteria.class);
            }
            return null;
        }

        public List<String> getCriteriaList(ObjectMapper mapper) {
            if (criteria instanceof List) {
                return mapper.convertValue(criteria, new TypeReference<List<String>>() {});
            }
            return null;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Criteria {
        private Integer greaterThan;
        private Integer lessThan;
        private List<String> include;
        private List<String> exclude;
    }
}
