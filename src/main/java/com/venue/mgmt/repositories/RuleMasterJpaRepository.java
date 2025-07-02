package com.venue.mgmt.repositories;

import com.venue.mgmt.dto.RuleMaster;
import com.venue.mgmt.entities.RuleMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RuleMasterJpaRepository extends JpaRepository<RuleMasterEntity, Long> {
    // Custom query methods can be defined here if needed
    // For example:

    Optional<RuleMasterEntity> findByRuleName(String ruleName);

    boolean existsByRuleName(String ruleName);

    Optional<RuleMaster> findByChannelCodeAndRuleName(String channelCode, String ruleName);

    List<RuleMaster> findByChannelCode(String channelCode);

    List<RuleMasterEntity> findByIsActiveTrue();

    boolean existsByChannelCodeAndRuleName(String channelCode, String ruleName);

    Optional<RuleMaster> getRuleByRuleNameAndChannelCode(String ruleName, String channelCode);

    void deleteByChannelCodeAndRuleName(String channelCode, String ruleName);
}
