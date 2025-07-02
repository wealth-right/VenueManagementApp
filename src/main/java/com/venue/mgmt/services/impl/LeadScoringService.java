package com.venue.mgmt.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.venue.mgmt.dto.*;
import com.venue.mgmt.entities.LeadDetailsEntity;
import com.venue.mgmt.entities.RuleMasterEntity;
import com.venue.mgmt.services.ILeadScoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LeadScoringService implements ILeadScoringService {

    private static final String HEAT_TAGGING_RULE = "heat_tagging_rules";
    private static final String HEAT_RULE_NOT_FOUND = "Heat tagging rules not found";
    private static final String SOURCE_RULES = "source_rules";
    private static final String RECENCY_RULES = "recency_rules";
    private static final String ENGAGEMENT_RULES = "engagement_rules";
    private final RuleMasterService ruleMasterService;

    private final LeadCrudService leadCrudService;

    public LeadScoringService(RuleMasterService ruleMasterService, LeadCrudService leadCrudService) {
        this.ruleMasterService = ruleMasterService;
        this.leadCrudService = leadCrudService;
    }

    /** {@inheritDoc} */
    @Override
    public int calculateLeadScore(LeadDetailsEntity leadDetails) {

        List<RuleMaster> allRules = ruleMasterService.getAllRules();
        log.debug("Calculating score for lead: {}", leadDetails.getEmail());

        int score = 0;

        List<String> actions = List.of("Link Clicked", "Each Email Opened", "Each Form Filled");
        // Add points for different scoring factors
        score += calculateDemographicScore(leadDetails.getAge(), leadDetails.getOccupation(), allRules);
        score += calculateScoreBasedOnSource(leadDetails.getSource(), allRules);
        score += calculateScoreBasedOnEngagement(actions, allRules);
        //    score+=calculateSpecificEngagementScore(
        //        leadDetails.getDob(),
        //        leadDetails.getFinancialDetails() != null
        //            ? leadDetails.getFinancialDetails().getIncomeRange()
        //            : null,
        //        actions,
        //        allRules);

        score += calculateScoreBasedOnRecency(leadDetails);

        log.debug("Calculated score {} for lead: {}", score, leadDetails.getEmail());
        return score;
    }

    @Override
    public String determineTemperature(int score) {
        Optional<RuleMasterEntity> ruleMaster = ruleMasterService.getRuleByName(HEAT_TAGGING_RULE);
        if (ruleMaster.isEmpty()) {
            log.error(HEAT_RULE_NOT_FOUND);
            return "COLD"; // Default temperature if rule not found
        }

        try {
            RuleMasterEntity rules = ruleMaster.get();
            ObjectMapper objectMapper = new ObjectMapper();
            HeatTaggingRules heatTaggingRules =
                    objectMapper.readValue(rules.getRuleValue(), HeatTaggingRules.class);
            log.debug("Parsed HeatTaggingRules: {}", heatTaggingRules);
            for (HeatTaggingRules.HeatTag heatTag : heatTaggingRules.getHeatTags()) {
                HeatTaggingRules.ScoreRange range = heatTag.getScoreRange();
                if (score >= range.getGreaterThanOrEqual() && score <= range.getLessThanOrEqual()) {
                    log.debug(
                            "Score {} falls within range: {} - {}",
                            score,
                            range.getGreaterThanOrEqual(),
                            range.getLessThanOrEqual());
                    return heatTag.getTag();
                }
            }
        } catch (Exception e) {
            log.error("Error parsing heat tagging rules: {}", e.getMessage());
        }
        // Default temperature if no rules match
        return "COLD";
    }

    @Scheduled(cron = "0 0 * * * *") // hourly
    @Transactional
    public void refreshLeadScoreAndTemperature() {
        List<LeadDetailsEntity> activeLeads =
                leadCrudService.findByStageNotIn(List.of("Customer", "Archived"));
        log.debug("Refreshing lead scores and temperatures for {} active leads", activeLeads.size());
        for (LeadDetailsEntity lead : activeLeads) {
            try {
                int updatedScore =
                        calculateScoreBasedOnRecency(lead) + lead.getScore(); // this includes recency
                String updatedTemperature = determineTemperature(updatedScore);

                lead.setScore(updatedScore);
                lead.setTemperature(updatedTemperature);
                log.debug(
                        "Lead: {} | Score: {} | Temperature: {}",
                        lead.getEmail(),
                        updatedScore,
                        updatedTemperature);
                leadCrudService.updateScoreAndTemperature(lead.getId(), updatedScore, updatedTemperature);
            } catch (Exception e) {
                log.error("Error processing lead {}: {}", lead.getId(), e.getMessage(), e);
            }
        }
        log.info("Lead score and temperature refresh complete.");
    }

    @Override
    public LeadScoreResponseDTO calculateLeadScoreAndTemperature(LeadDetailsEntity leadDetails) {
        log.debug("Calculating lead score and temperature for: {}", leadDetails.getEmail());
        int score = calculateLeadScore(leadDetails);
        String temperature = determineTemperature(score);
        return new LeadScoreResponseDTO(score, temperature);
    }

    private int calculateScoreBasedOnRecency(LeadDetailsEntity leadDetails) {
        Optional<RuleMasterEntity> ruleMaster = ruleMasterService.getRuleByName(RECENCY_RULES);
        if (ruleMaster.isEmpty()) {
            log.error("Recency rules not found");
            return 0;
        }

        try {
            RuleMasterEntity rulesEntity = ruleMaster.get();
            ObjectMapper objectMapper = new ObjectMapper();
            RecencyRules recencyRules =
                    objectMapper.readValue(rulesEntity.getRuleValue(), RecencyRules.class);
            log.debug("Parsed RecencyRules: {}", recencyRules);

            LocalDateTime createdAt = leadDetails.getCreatedAt();
            long daysSinceCreated = 0;
            if (createdAt != null) {
                daysSinceCreated = ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDate.now());
            }

            for (RecencyRules.RecencyRule rule : recencyRules.getRecency()) {
                RecencyRules.TimePeriod timePeriod = rule.getTimePeriod();
                if ((timePeriod.getGreaterThan() == null || daysSinceCreated > timePeriod.getGreaterThan())
                        && (timePeriod.getLessThan() == null || daysSinceCreated < timePeriod.getLessThan())) {
                    log.debug("Recency {} days matches rule: {}", daysSinceCreated, rule.getType());
                    return rule.getScore();
                }
            }
        } catch (Exception e) {
            log.error("Error parsing recency rules: {}", e.getMessage());
        }

        return 0;
    }


    private int calculateScoreBasedOnRecency(LeadDetails leadDetails) {
        Optional<RuleMasterEntity> ruleMaster = ruleMasterService.getRuleByName(RECENCY_RULES);
        if (ruleMaster.isEmpty()) {
            log.error("Recency rules not found");
            return 0;
        }

        try {
            RuleMasterEntity rulesEntity = ruleMaster.get();
            ObjectMapper objectMapper = new ObjectMapper();
            RecencyRules recencyRules =
                    objectMapper.readValue(rulesEntity.getRuleValue(), RecencyRules.class);
            log.debug("Parsed RecencyRules: {}", recencyRules);

            LocalDateTime createdAt = leadDetails.getCreatedAt();
            long daysSinceCreated = 0;
            if (createdAt != null) {
                daysSinceCreated = ChronoUnit.DAYS.between(createdAt.toLocalDate(), LocalDate.now());
            }

            for (RecencyRules.RecencyRule rule : recencyRules.getRecency()) {
                RecencyRules.TimePeriod timePeriod = rule.getTimePeriod();
                if ((timePeriod.getGreaterThan() == null || daysSinceCreated > timePeriod.getGreaterThan())
                        && (timePeriod.getLessThan() == null || daysSinceCreated < timePeriod.getLessThan())) {
                    log.debug("Recency {} days matches rule: {}", daysSinceCreated, rule.getType());
                    return rule.getScore();
                }
            }
        } catch (Exception e) {
            log.error("Error parsing recency rules: {}", e.getMessage());
        }

        return 0;
    }

    private int calculateScoreBasedOnEngagement(List<String> actions, List<RuleMaster> allRules) {
        if (actions == null || actions.isEmpty() || allRules == null || allRules.isEmpty()) {
            return 0;
        }
        Optional<RuleMasterEntity> ruleMaster = ruleMasterService.getRuleByName(ENGAGEMENT_RULES);
        if (ruleMaster.isEmpty()) {
            log.error("Engagement rules not found");
            return 0;
        }
        try {
            RuleMasterEntity rulesEntity = ruleMaster.get();
            ObjectMapper objectMapper = new ObjectMapper();
            EngagementRules engagementRules =
                    objectMapper.readValue(rulesEntity.getRuleValue(), EngagementRules.class);
            log.debug("Parsed EngagementRules: {}", engagementRules);

            int score = 0;

            // Calculate score based on actions
            for (String action : actions) {
                for (EngagementRules.EngagementRule rule : engagementRules.getEngagement()) {
                    if (rule.getAction().contains(action)) {
                        log.debug("Action {} matches rule: {}", action, rule.getAction());
                        score += rule.getScore();
                    }
                }
            }
            return score;
        } catch (Exception e) {
            log.error("Error parsing engagement rules: {}", e.getMessage());
        }

        return 0; // Default score if no match is found or an error occurs
    }

    private int calculateAgeFromDOB(LocalDate dob) {
        if (dob == null) return 0;
        return Period.between(dob, LocalDate.now()).getYears();
    }

    private int calculateSpecificEngagementScore(
            LocalDate dob, Long income, List<String> actions, List<RuleMaster> allRules) {
        if ((dob == null && income == null) && (actions == null || actions.isEmpty())
                || allRules == null
                || allRules.isEmpty()) {
            return 0;
        }
        Optional<RuleMasterEntity> ruleMasterOpt =
                ruleMasterService.getRuleByName("specific_engagement_scores");
        if (ruleMasterOpt.isEmpty()) {
            log.error("Specific engagement rules not found");
            return 0;
        }
        try {
            int age = calculateAgeFromDOB(dob);
            RuleMasterEntity ruleMaster = ruleMasterOpt.get();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(ruleMaster.getRuleValue());
            JsonNode ruleArray = rootNode.get("specific_engagement_scores");

            int totalScore = 0;
            for (JsonNode rule : ruleArray) {
                String event = rule.get("event").asText();

                // Match exact event
                if (actions != null
                        && actions.stream().anyMatch(action -> action.equalsIgnoreCase(event))) {
                    int score = rule.get("score_change").asInt();
                    log.debug("Matched event '{}', adding score: {}", event, score);
                    totalScore += score;
                    continue;
                }
                // Match Age + Income rule
                if (event.equalsIgnoreCase("Age and Income Match") && age > 0 && income != null) {
                    JsonNode criteria = rule.get("criteria");
                    JsonNode ageCriteria = criteria.get("age");
                    JsonNode incomeCriteria = criteria.get("income");

                    boolean ageOk = true;
                    boolean incomeOk = true;

                    if (ageCriteria != null) {
                        if (ageCriteria.has("greaterThan")) {
                            ageOk = age > ageCriteria.get("greaterThan").asInt();
                        }
                        if (ageCriteria.has("lessThan")) {
                            ageOk = ageOk && age < ageCriteria.get("lessThan").asInt();
                        }
                    }
                    if (incomeCriteria != null && incomeCriteria.has("greaterThan")) {
                        incomeOk = income > incomeCriteria.get("greaterThan").asLong();
                    }
                    if (ageOk && incomeOk) {
                        int score = rule.get("score_change").asInt();
                        log.debug("Matched Age & Income rule, adding score: {}", score);
                        totalScore += score;
                    }
                }
            }
            return totalScore;
        } catch (Exception e) {
            log.error("Error calculating specific engagement score: {}", e.getMessage(), e);
        }
        return 0;
    }

    private int calculateScoreBasedOnSource(String source, List<RuleMaster> rules) {
        if (source == null || rules == null || rules.isEmpty()) {
            return 0;
        }
        Optional<RuleMasterEntity> ruleMaster = ruleMasterService.getRuleByName(SOURCE_RULES);
        if (ruleMaster.isEmpty()) {
            log.error("Source rules not found");
            return 0;
        }
        try {
            RuleMasterEntity rulesEntity = ruleMaster.get();
            ObjectMapper objectMapper = new ObjectMapper();
            SourceRules sourceRules =
                    objectMapper.readValue(rulesEntity.getRuleValue(), SourceRules.class);
            log.debug("Parsed SourceRules: {}", sourceRules);
            for (SourceRules.SourceRule rule : sourceRules.getSource()) {
                if (rule.getChannel().equalsIgnoreCase(source)) {
                    log.debug("Source {} matches rule: {}", source, rule.getChannel());
                    return rule.getScore();
                }
            }

        } catch (Exception e) {
            log.error("Error parsing source rules: {}", e.getMessage());
        }

        return 0;
    }

    /**
     * Calculates score component based on age.
     *
     * @param age age of the lead
     * @return Score points for age
     */
    private int calculateDemographicScore(
            int age, String occupation, List<RuleMaster> allRules) {
        if (allRules == null || allRules.isEmpty()) {
            return 0;
        }
        try {
            int score = 0;
            score += calculateAllDemographichCriteriaScore(allRules, age, occupation);

            return score;
        } catch (Exception e) {
            log.error("Error calculating age score: {}", e.getMessage());
            return 0; // Default score if error occurs
        }
    }

    /**
     * Calculates score based on all demographic criteria.
     *
     * @param allRules List of all rules
     * @param age Age of the lead
     * @return Score points for demographic criteria
     */
    private int calculateAllDemographichCriteriaScore(
            List<RuleMaster> allRules, int age, String occupation) {
        for (RuleMaster rule : allRules) {
            if ("demographic_rules".equalsIgnoreCase(rule.getRuleName())) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    DemographicRules demographicRules =
                            objectMapper.readValue(rule.getRuleValue(), DemographicRules.class);
                    int score = 0;
                    for (DemographicRules.DemographicRule demoRule : demographicRules.getDemographic()) {
                        score += calculateRuleScore(demoRule, age, occupation, objectMapper);
                    }
                    return score;
                } catch (Exception e) {
                    log.error("Error parsing demographic rules: {}", e.getMessage());
                }
            }
        }
        return 0;
    }

    private int calculateRuleScore(
            DemographicRules.DemographicRule demoRule,
            int age,
            String occupation,
            ObjectMapper objectMapper) {
        return switch (demoRule.getAttribute()) {
            case "Age Group" -> calculateAgeGroupScore(demoRule, age, objectMapper);
            case "City Tier" -> calculateCityTierScore(demoRule, objectMapper); // doubt
            case "Occupation" -> calculateOccupationScore(demoRule, occupation, objectMapper);
            case "Income Bracket" -> calculateIncomeBracketScore(demoRule, objectMapper); // doubt
            default -> 0;
        };
    }

    private int calculateAgeGroupScore(
            DemographicRules.DemographicRule demoRule, int age, ObjectMapper objectMapper) {
        try {
            DemographicRules.Criteria criteria = demoRule.getCriteriaObject(objectMapper);
            Integer gt = criteria.getGreaterThan(); // 25
            Integer lt = criteria.getLessThan(); // 45

            if ((gt == null || age > gt) && (lt == null || age < lt)) {
                return demoRule.getScore(); // 5
            }
        } catch (Exception e) {
            log.error("Error calculating Age Group score: {}", e.getMessage());
        }
        return 0;
    }

    private int calculateCityTierScore(
            DemographicRules.DemographicRule demoRule, ObjectMapper objectMapper) {
        try {
            List<String> cityCriteria = demoRule.getCriteriaList(objectMapper);
            if (cityCriteria != null && cityCriteria.contains(demoRule.getCriteria())) {
                return demoRule.getScore();
            }
        } catch (Exception e) {
            log.error("Error calculating City Tier score: {}", e.getMessage());
        }
        return 0;
    }

    private int calculateOccupationScore(
            DemographicRules.DemographicRule demoRule, String occupation, ObjectMapper objectMapper) {
        try {
            DemographicRules.Criteria criteria = demoRule.getCriteriaObject(objectMapper);
            List<String> excludeList = criteria.getExclude();
            if (excludeList != null
                    && !excludeList.contains(occupation)) { // Replace "Retired" with the lead's occupation
                return demoRule.getScore();
            }
        } catch (Exception e) {
            log.error("Error calculating Occupation score: {}", e.getMessage());
        }
        return 0;
    }

    private int calculateIncomeBracketScore(
            DemographicRules.DemographicRule demoRule, ObjectMapper objectMapper) {
        try {
            DemographicRules.Criteria criteria = demoRule.getCriteriaObject(objectMapper);
            Integer incomeGreaterThan = criteria.getGreaterThan();
            if (incomeGreaterThan != null
                    && 1500000 > incomeGreaterThan) { // Replace 1500000 with the lead's income
                return demoRule.getScore();
            }
        } catch (Exception e) {
            log.error("Error calculating Income Bracket score: {}", e.getMessage());
        }
        return 0;
    }
}
