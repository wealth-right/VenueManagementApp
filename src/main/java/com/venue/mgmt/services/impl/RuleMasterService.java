package com.venue.mgmt.services.impl;

import com.venue.mgmt.dto.RuleMapperImpl;
import com.venue.mgmt.dto.RuleMaster;
import com.venue.mgmt.dto.RuleMasterDTO;
import com.venue.mgmt.entities.RuleMasterEntity;
import com.venue.mgmt.exception.RuleAlreadyExistsException;
import com.venue.mgmt.exception.RuleNotFoundException;
import com.venue.mgmt.repositories.IRuleMasterRepository;
import com.venue.mgmt.services.IRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class RuleMasterService implements IRuleService {

    private final IRuleMasterRepository ruleMasterRepository;

    // Cache metrics

    public RuleMasterService(IRuleMasterRepository ruleMasterRepository) {
        this.ruleMasterRepository = ruleMasterRepository;
    }

    // Get all rules
    @Override
    @Cacheable(value = "rulesCache")
    public List<RuleMaster> getAllRules() {
        log.info("Fetching rules from the database...");
        return ruleMasterRepository.findByIsActiveTrue().stream().map(this::mapToDomain).toList();
    }

    private RuleMaster mapToDomain(RuleMasterEntity entity) {
        return new RuleMaster(
                entity.getId(),
                entity.getRuleName(),
                entity.getChannelCode(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getRuleValue(),
                entity.getIsActive());
    }

    @Override
    public Optional<RuleMasterEntity> getRuleByName(String ruleName) {
        return ruleMasterRepository.findByRuleName(ruleName);
    }

    @Override
    public Optional<RuleMaster> getRuleByRuleNameAndChannelCode(String ruleName, String channelCode) {
        return ruleMasterRepository.getRuleByRuleNameAndChannelCode(ruleName, channelCode);
    }

    @CacheEvict(value = "rulesCache", allEntries = true)
    public RuleMaster createRule(String channelCode, String ruleName, String ruleValue) {
        log.info("New rule added — evicting cache");
        if (ruleMasterRepository.existsByChannelCodeAndRuleName(channelCode, ruleName)) {
            throw new RuleAlreadyExistsException(
                    "Rule with name '" + ruleName + "' already exists for channel '" + channelCode + "'");
        }
        RuleMaster rule = new RuleMaster();
        rule.setRuleName(ruleName);
        rule.setRuleValue(ruleValue);
        RuleMapperImpl ruleMapper = new RuleMapperImpl();
        RuleMasterDTO ruleDTO = ruleMapper.toDTO(rule);
        return ruleMasterRepository.save(ruleDTO);
    }

    @Override
    @CacheEvict(value = "rulesCache", allEntries = true)
    public RuleMaster updateRule(String channelCode, String ruleName, String ruleValue) {
        Optional<RuleMaster> ruleOptional =
                ruleMasterRepository.findByChannelCodeAndRuleName(channelCode, ruleName);
        if (ruleOptional.isPresent()) {
            RuleMaster rule = ruleOptional.get();
            rule.setRuleValue(ruleValue);
            RuleMapperImpl ruleMapper = new RuleMapperImpl();
            RuleMasterDTO ruleDTO = ruleMapper.toDTO(rule);
            return ruleMasterRepository.save(ruleDTO);
        }
        throw new RuleNotFoundException(
                "Rule not found for channel '" + channelCode + "' and name '" + ruleName + "'");
    }

    public void deleteRule(String channelCode, String ruleName) {
        //        set as soft delete by setting status inactive.
        ruleMasterRepository.deleteByChannelCodeAndRuleName(channelCode, ruleName);
    }
}
