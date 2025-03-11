package com.venue.mgmt.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.venue.mgmt.enums.ProductType;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@DynamicUpdate
@Table(name = "lead_registration", schema = "public")
public class LeadRegistration extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lead_registration_seq")
    @SequenceGenerator(name = "lead_registration_seq", sequenceName = "lead_registration_lead_id_seq", allocationSize = 1)
    @Column(name = "lead_id")
    Long leadId;

    @Column(name = "full_name", nullable = false)
    @NotNull
    String fullName;

    @Column(name = "age")
    int age;

    @Column(name = "dob")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate dob;

    @Column(name = "occupation")
    String occupation;

    @Column(name = "income_range")
    String incomeRange;

    @Column(name = "mobile_number")
    String mobileNumber;

    @ElementCollection
    @CollectionTable(
            name = "lead_existing_products",
            joinColumns = @JoinColumn(name = "lead_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private Set<ProductType> existingProducts = new HashSet<>();

    @Column(name = "life_stage")
    private String lifeStage;

    @Column(name = "address")
    String address;

    @Column(name = "pin_code")
    String pinCode;

    @Column(name = "gender")
    String gender;

    @Column(name = "email")
    String email;

//    @OneToOne(mappedBy = "leadRegistration", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//    @JsonManagedReference
//    private Campaign campaignEntity;

//    @Column(name = "line_of_business")
//    String lineOfBusiness;

    @Column(name = "status")
    String status;

    @Column(name = "remarks")
    String remarks;

    @Column(name = "marital_status")
    String maritalStatus;

    @Column(name = "is_active")
    Boolean isActive = true;

    @Column(name = "is_deleted")
    Boolean isDeleted = false;

    @Column(name = "is_verified")
    Boolean isVerified = false;

//    private Long venueId; // Add this field


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    @JsonBackReference
    private Venue venue;

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

//    public Long getVenueId() {
//        return venueId;
//    }
//
//    public void setVenueId(Long venueId) {
//        this.venueId = venueId;
//    }
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getOccupation() {
        return occupation;
    }

    public void setOccupation(String occupation) {
        this.occupation = occupation;
    }

    public String getIncomeRange() {
        return incomeRange;
    }

    public void setIncomeRange(String incomeRange) {
        this.incomeRange = incomeRange;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Set<ProductType> getExistingProducts() {
        return existingProducts;
    }

    public void setExistingProducts(Set<ProductType> existingProducts) {
        this.existingProducts = existingProducts;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLifeStage() {
        return lifeStage;
    }

    public void setLifeStage(String lifeStage) {
        this.lifeStage = lifeStage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getMaritalStatus() {
        return maritalStatus;
    }

    public void setMaritalStatus(String maritalStatus) {
        this.maritalStatus = maritalStatus;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Boolean getVerified() {
        return isVerified;
    }

    public void setVerified(Boolean verified) {
        isVerified = verified;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }


    @AssertTrue(message = "Either mobile number or email must be provided")
    public boolean isEitherMobileOrEmailPresent() {
        return (mobileNumber != null && !mobileNumber.trim().isEmpty()) ||
                (email != null && !email.trim().isEmpty());
    }
}
