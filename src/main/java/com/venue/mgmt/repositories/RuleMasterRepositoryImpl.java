package com.venue.mgmt.repositories;

import com.venue.mgmt.dto.RuleMapperImpl;
import com.venue.mgmt.dto.RuleMaster;
import com.venue.mgmt.dto.RuleMasterDTO;
import com.venue.mgmt.entities.RuleMasterEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RuleMasterRepositoryImpl implements IRuleMasterRepository {

    private final RuleMasterJpaRepository ruleMasterJpaRepository;

    public RuleMasterRepositoryImpl(RuleMasterJpaRepository ruleMasterJpaRepository) {
        this.ruleMasterJpaRepository = ruleMasterJpaRepository;
    }

    @Override
    public Optional<RuleMasterEntity> findByRuleName(String ruleName) {
        return ruleMasterJpaRepository.findByRuleName(ruleName);
    }

    @Override
    public boolean existsByRuleName(String ruleName) {
        return ruleMasterJpaRepository.existsByRuleName(ruleName);
    }

    @Override
    public List<RuleMasterEntity> findByIsActiveTrue() {
        return ruleMasterJpaRepository.findByIsActiveTrue().stream()
                .map(
                        entity ->
                                new RuleMasterEntity(
                                        entity.getId(),
                                        entity.getRuleName(),
                                        entity.getRuleValue(),
                                        entity.getChannelCode(),
                                        entity.getUpdatedAt(),
                                        entity.getCreatedAt(),
                                        entity.getIsActive()))
                .toList();
    }

    @Override
    public Optional<RuleMaster> getRuleByRuleNameAndChannelCode(String ruleName, String channelCode) {
        return ruleMasterJpaRepository
                .getRuleByRuleNameAndChannelCode(ruleName, channelCode)
                .map(
                        entity ->
                                new RuleMaster(
                                        entity.getId(),
                                        entity.getRuleName(),
                                        entity.getRuleValue(),
                                        entity.getCreatedAt(),
                                        entity.getUpdatedAt(),
                                        entity.getChannelCode(),
                                        entity.getIsActive()));
    }

    @Override
    public RuleMaster save(RuleMasterDTO rule) {
        RuleMapperImpl ruleMapper = new RuleMapperImpl();
        RuleMaster ruleMaster = ruleMapper.toDomain(rule);
        RuleMasterEntity savedEntity =
                ruleMasterJpaRepository.save(
                        new RuleMasterEntity(
                                ruleMaster.getId(),
                                ruleMaster.getRuleName(),
                                ruleMaster.getRuleValue(),
                                ruleMaster.getChannelCode(),
                                ruleMaster.getCreatedAt(),
                                ruleMaster.getUpdatedAt(),
                                ruleMaster.getIsActive()));

        return new RuleMaster(
                savedEntity.getId(),
                savedEntity.getRuleName(),
                savedEntity.getRuleValue(),
                savedEntity.getCreatedAt(),
                savedEntity.getUpdatedAt(),
                savedEntity.getChannelCode(),
                savedEntity.getIsActive());
    }

    @Override
    public Optional<RuleMaster> findByChannelCodeAndRuleName(String channelCode, String ruleName) {
        return ruleMasterJpaRepository
                .findByChannelCodeAndRuleName(channelCode, ruleName)
                .map(
                        entity ->
                                new RuleMaster(
                                        entity.getId(),
                                        entity.getRuleName(),
                                        entity.getRuleValue(),
                                        entity.getCreatedAt(),
                                        entity.getUpdatedAt(),
                                        entity.getChannelCode(),
                                        entity.getIsActive()));
    }

    @Override
    public List<RuleMaster> findByChannelCode(String channelCode) {
        return ruleMasterJpaRepository.findByChannelCode(channelCode).stream()
                .map(
                        entity ->
                                new RuleMaster(
                                        entity.getId(),
                                        entity.getRuleName(),
                                        entity.getRuleValue(),
                                        entity.getCreatedAt(),
                                        entity.getUpdatedAt(),
                                        entity.getChannelCode(),
                                        entity.getIsActive()))
                .toList();
    }

    @Override
    public boolean existsByChannelCodeAndRuleName(String channelCode, String ruleName) {
        return ruleMasterJpaRepository.existsByChannelCodeAndRuleName(channelCode, ruleName);
    }

    @Override
    public void deleteByChannelCodeAndRuleName(String channelCode, String ruleName) {
        ruleMasterJpaRepository.deleteByChannelCodeAndRuleName(channelCode, ruleName);
    }
}