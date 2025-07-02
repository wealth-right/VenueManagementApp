package com.venue.mgmt.dto;

import com.venue.mgmt.entities.AddressDetailsEntity;
import com.venue.mgmt.enums.ProductType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class LeadDetails {
    private Long id;

    private String title;

    private String firstName;

    private String middleName;

    private String lastName;

    private String fullName;

    private String lifeStage;

    private String lineOfBusiness;

    private String mobileNumber;

    private String pinCode;

    private String customerId;

    private String status;

    private String remarks;

    private String email;

    private Long venueId;

    private String countryCode;

    private String phoneNumber;

    private String lifeStageMaritalStatus;

    private Integer noOfDependents;

    private LocalDate dob;

    private Integer age;

    private Boolean isActive = true;

    private Boolean isDeleted = false;

    private Boolean isMobileVerified = false;
    private String assignedToTca;
    private String occupation;

    private String gender;

    private String taxStatus;

    private String nationality;

    private String education;

    private String aadhaar;

    private String pan;

    private String stage;

    private Integer score;

    private String temperature;

    private String source;

    private String assignedTo;

    private String roleCode;

    private String channelCode;

    private LocalDateTime createdAt;

    private String createdBy;

    protected Date lastModifiedAt;

    private AddressDetailsEntity addressDetails;

    private List<BankDetails> bankDetails;

    private FinancialDetails financialDetails;

    private ProductType productType; // New field

    private String branchCode; // New Field to accomodate branch assignment
}
