package com.venue.mgmt.dto;

public class RuleMapperImpl {
    public RuleMasterDTO toDTO(RuleMaster ruleMaster) {
        if (ruleMaster == null) {
            return null;
        }
        return RuleMasterDTO.builder()
                .id(ruleMaster.getId())
                .ruleName(ruleMaster.getRuleName())
                .ruleValue(ruleMaster.getRuleValue())
                .createdAt(ruleMaster.getCreatedAt())
                .updatedAt(ruleMaster.getUpdatedAt())
                .channelCode(ruleMaster.getChannelCode())
                .build();
    }

    public RuleMaster toDomain(RuleMasterDTO ruleMasterDTO) {
        if (ruleMasterDTO == null) {
            return null;
        }
        return RuleMaster.builder()
                .id(ruleMasterDTO.getId())
                .ruleName(ruleMasterDTO.getRuleName())
                .ruleValue(ruleMasterDTO.getRuleValue())
                .createdAt(ruleMasterDTO.getCreatedAt())
                .updatedAt(ruleMasterDTO.getUpdatedAt())
                .channelCode(ruleMasterDTO.getChannelCode())
                .build();
    }
}
