package com.venue.mgmt.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.venue.mgmt.enums.ProductType;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@DynamicUpdate
@Table(name = "lead_details", schema = "leadmgmt")
public class LeadRegistration extends Auditable<String> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    Long leadId;

    @Column(name = "full_name", nullable = false)
    @NotNull
    String fullName;

    @Column(name = "age",nullable = false)
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

    @Column(name="phone_number")
    String phoneNumber;

    @Column(name="customer_id")
    String customerId;


    @ElementCollection
    @CollectionTable(
            name = "lead_existing_products",
            schema = "leadmgmt",
            joinColumns = @JoinColumn(name = "lead_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private Set<ProductType> existingProducts = new HashSet<>();

    @Column(name = "life_stage")
    private String lifeStage;

    @Column(name = "line_of_business")
    private String lineOfBusiness;


    @Column(name = "address")
    String address;

    @Column(name = "pin_code")
    String pinCode;

    @Column(name = "gender")
    String gender;

    @Column(name = "email")
    String email;


    @Column(name = "status")
    String status;

    @Column(name = "remarks")
    String remarks;

    @Column(name="source")
    String source;

    @Column(name = "marital_status")
    String maritalStatus;

    @Transient
    private String lifeStageMaritalStatus;

    @Column(name = "lead_score")
    private Integer score;

    @Column(name = "lead_temperature")
    private String temperature;

    @Column(name = "title")
    private String title;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "tax_status")
    private String taxStatus;

    @Column(name = "education")
    private String education;

    @Column(name = "aadhaar")
    private String aadhaar;

    @Column(name = "pan")
    private String pan;

    @Column(name = "stage")
    private String stage;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "role_code")
    private String roleCode;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "channel_code")
    private String channelCode;


    @Column(name = "is_active")
    Boolean isActive = true;

    @Column(name = "is_deleted")
    Boolean isDeleted = false;

    @Column(name = "is_mobile_verified")
    Boolean isMobileVerified = false;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    @JsonBackReference
    private Venue venue;

    public String getLifeStageMaritalStatus() {
        return lifeStageMaritalStatus;
    }

    public void setLifeStageMaritalStatus(String lifeStageMaritalStatus) {
        this.lifeStageMaritalStatus = lifeStageMaritalStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public void setTaxStatus(String taxStatus) {
        this.taxStatus = taxStatus;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public void setAadhaar(String aadhaar) {
        this.aadhaar = aadhaar;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    public void setChannelCode(String channelCode) {
        this.channelCode = channelCode;
    }

    public Integer getScore() {
        return score;
    }

    public String getTemperature() {
        return temperature;
    }

    public String getTitle() {
        return title;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getNationality() {
        return nationality;
    }

    public String getTaxStatus() {
        return taxStatus;
    }

    public String getEducation() {
        return education;
    }

    public String getAadhaar() {
        return aadhaar;
    }

    public String getPan() {
        return pan;
    }

    public String getStage() {
        return stage;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public String getChannelCode() {
        return channelCode;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public Long getLeadId() {
        return leadId;
    }

    public void setLeadId(Long leadId) {
        this.leadId = leadId;
    }

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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
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

    public Boolean getMobileVerified() {
        return isMobileVerified;
    }

    public void setMobileVerified(Boolean verified) {
        isMobileVerified = verified;
    }

    public Venue getVenue() {
        return venue;
    }

    public void setVenue(Venue venue) {
        this.venue = venue;
    }

    public String getLineOfBusiness() {
        return lineOfBusiness;
    }

    public void setLineOfBusiness(String lineOfBusiness) {
        this.lineOfBusiness = lineOfBusiness;
    }


    @AssertTrue(message = "Either mobile number or email must be provided")
    public boolean isEitherMobileOrEmailPresent() {
        return (mobileNumber != null && !mobileNumber.trim().isEmpty()) ||
                (email != null && !email.trim().isEmpty());
    }
}
