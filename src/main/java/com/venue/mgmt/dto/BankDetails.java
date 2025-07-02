package com.venue.mgmt.dto;

import com.venue.mgmt.entities.LeadDetailsEntity;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankDetails {

    private Long id;

    private Long customerId;

    private String bankName;

    private String accountNumber;

    private String accountHolderName;

    private String ifscCode;

    private String accountType;

    private String micrCode;

    private String branchAddress;

    private LeadDetails leadDetails;
}
