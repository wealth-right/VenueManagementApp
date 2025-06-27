package com.venue.mgmt.entities;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "address_details")
public class AddressDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "address_id")
    private Long id;

    // Permanent Address
    @Column(name = "permanent_address_line1")
    private String permanentAddressLine1;

    @Column(name = "permanent_address_line2")
    private String permanentAddressLine2;

    @Column(name = "permanent_city")
    private String permanentCity;

    @Column(name = "permanent_state")
    private String permanentState;

    @Column(name = "permanent_country")
    private String permanentCountry;

    @Column(name = "permanent_pincode")
    private String permanentPincode;

    // Communication Address
    @Column(name = "communication_address_line1")
    private String communicationAddressLine1;

    @Column(name = "communication_address_line2")
    private String communicationAddressLine2;

    @Column(name = "communication_city")
    private String communicationCity;

    @Column(name = "communication_state")
    private String communicationState;

    @Column(name = "communication_country")
    private String communicationCountry;

    @Column(name = "communication_pincode")
    private String communicationPincode;

    @OneToOne(mappedBy = "addressDetailsEntity", cascade = CascadeType.ALL)
    private LeadDetailsEntity leadDetailsEntity;
}
