package com.venue.mgmt.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RuleMaster {
    private Long id;

    private String ruleName;

    private String ruleValue;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private String channelCode;
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Instead of raw string, expose parsed JSON directly
    @JsonProperty("ruleValue")
    public Map<String, Object> getParsedRuleValue() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.ruleValue, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of("error", "Invalid JSON");
        }
    }
}