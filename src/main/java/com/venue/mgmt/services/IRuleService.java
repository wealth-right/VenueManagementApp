package com.venue.mgmt.services;

import com.venue.mgmt.dto.RuleMaster;
import com.venue.mgmt.entities.RuleMasterEntity;

import java.util.List;
import java.util.Optional;

public interface IRuleService {
    Optional<RuleMasterEntity> getRuleByName(String ruleName);

    List<RuleMaster> getAllRules();

    Optional<RuleMaster> getRuleByRuleNameAndChannelCode(String ruleName, String channelCode);

    RuleMaster createRule(String channelCode, String ruleName, String ruleValue);

    RuleMaster updateRule(String channelCode, String ruleName, String ruleValue);

    public void deleteRule(String channelCode, String ruleName);
}
