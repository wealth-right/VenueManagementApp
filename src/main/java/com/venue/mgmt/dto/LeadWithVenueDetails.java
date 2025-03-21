package com.venue.mgmt.dto;

import com.venue.mgmt.enums.ProductType;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class LeadWithVenueDetails {
    private Long leadId;
    private String fullName;
    private int age;
    private String occupation;
    private String mobileNumber;
    private String address;
    private String email;
    private boolean active;
    private boolean verified;
    private boolean eitherMobileOrEmailPresent;
    private String createdBy;
    private String creationDate;
    private String lastModifiedBy;
    private String lastModifiedDate;
    private String incomeRange;
    private String lifeStage;
    private String lineOfBusiness;
    private String gender;
    private String remarks;
    private String maritalStatus;
    private boolean deleted;
    private Set<ProductType> existingProducts;
    private VenueDetails venueDetails;

    // Getters and setters
    @Getter
    @Setter
    public static class VenueDetails {
        private Long venueId;
        private String venueName;
        private double latitude;
        private double longitude;
        private boolean isActive;
        private String address;

        // Getters and setters
    }
}
