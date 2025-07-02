package com.venue.mgmt.services;

import com.venue.mgmt.dto.LeadScoreResponseDTO;
import com.venue.mgmt.entities.LeadDetailsEntity;

public interface ILeadScoringService {

    /**
     * Calculates and sets the score and temperature for a lead.
     *
     * @param leadDetails The lead to score
     * @return The calculated score
     */
    int calculateLeadScore(LeadDetailsEntity leadDetails);

    /**
     * Determines the appropriate temperature category based on score.
     *
     * @param score The lead score
     * @return The temperature category (e.g., "HOT", "WARM", "COLD")
     */
    String determineTemperature(int score);

    void refreshLeadScoreAndTemperature();

    public LeadScoreResponseDTO calculateLeadScoreAndTemperature(LeadDetailsEntity leadDetails);
}
