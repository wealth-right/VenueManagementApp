package com.venue.mgmt.services.impl;

import com.venue.mgmt.entities.LeadDetailsEntity;
import com.venue.mgmt.repositories.LeadDetailsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeadCrudService {

    private final LeadDetailsRepository leadRepository;

    public List<LeadDetailsEntity> findByStageNotIn(List<String> leadsStage) {
        log.debug("Retrieving leads with stage not in: {}", leadsStage);
        List<LeadDetailsEntity> leads = leadRepository.findByStageNotIn(leadsStage);
        log.debug("Retrieved {} leads", leads.size());
        return leads;
    }


    public void updateScoreAndTemperature(Long leadId, int updatedScore, String updatedTemperature) {
        log.debug(
                "Updating score and temperature for lead with ID: {}, Score: {}, Temperature: {}",
                leadId,
                updatedScore,
                updatedTemperature);

        leadRepository.updatedTemperature(leadId, updatedScore, updatedTemperature);
    }
}
