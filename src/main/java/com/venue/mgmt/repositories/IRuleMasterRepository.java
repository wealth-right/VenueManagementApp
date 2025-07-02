package com.venue.mgmt.repositories;

import com.venue.mgmt.dto.RuleMaster;
import com.venue.mgmt.dto.RuleMasterDTO;
import com.venue.mgmt.entities.RuleMasterEntity;

import java.util.List;
import java.util.Optional;

public interface IRuleMasterRepository {

    Optional<RuleMasterEntity> findByRuleName(String ruleName);

    boolean existsByRuleName(String ruleName);

    List<RuleMasterEntity> findByIsActiveTrue();


    Optional<RuleMaster> getRuleByRuleNameAndChannelCode(String ruleName, String channelCode);

    RuleMaster save(RuleMasterDTO rule);

    Optional<RuleMaster> findByChannelCodeAndRuleName(String channelCode, String ruleName);

    List<RuleMaster> findByChannelCode(String channelCode);

    boolean existsByChannelCodeAndRuleName(String channelCode, String ruleName);

    void deleteByChannelCodeAndRuleName(String channelCode, String ruleName);
}
