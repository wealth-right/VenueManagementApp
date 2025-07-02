package com.venue.mgmt.dto;

import com.venue.mgmt.entities.LeadDetailsEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialDetails {

    private Long id;

    private String savings;

    private String loans;

    private String monthlyExpense;

    private String incomeRange;

    private boolean existingHealthInsurance;

    private boolean existingLifeInsurance;

    private LeadDetails leadDetails;
}
