package com.venue.mgmt.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.venue.mgmt.enums.ProductType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "lead_details")
public class LeadDetailsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lead_id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "full_name")
    String fullName;

    @Column(name = "life_stage")
    private String lifeStage;

    @Column(name = "line_of_business")
    private String lineOfBusiness;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "pin_code")
    String pinCode;

    @Column(name = "customer_id")
    String customerId;

    @Column(name = "status")
    String status;

    @Column(name = "remarks")
    String remarks;

    @Column(name = "email", unique = true)
    private String email;


    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date creationDate;


    @Column(name = "country_code")
    private String countryCode;

    String lastModifiedBy;

    Date lastModifiedDate;

    @Column(name = "income_range")
    private String incomeRange;

    // Existing products
    @ElementCollection
    @CollectionTable(
            name = "lead_existing_products",
            schema = "leadmgmt",
            joinColumns = @JoinColumn(name = "lead_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "product_type")
    private Set<ProductType> existingProducts = new HashSet<>();

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "marital_status")
    private String lifeStageMaritalStatus;

    @Column(name = "no_of_dependents")
    private Integer noOfDependents;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "age")
    Integer age;

    @Column(name = "is_active")
    Boolean isActive = true;

    @Column(name = "is_deleted")
    Boolean isDeleted = false;

    @Column(name = "is_mobile_verified")
    Boolean isMobileVerified = false;

    @Column(name = "assigned_to_tca")
    String assignedToTca;

    @Column(name = "occupation")
    private String occupation;

    @Column(name = "gender")
    private String gender;

    @Column(name = "tax_status")
    private String taxStatus;

    @Column(name = "nationality")
    private String nationality;

    @Column(name = "education")
    private String education;

    @Column(name = "aadhaar")
    private String aadhaar;

    @Column(name = "pan")
    private String pan;

    @Column(name = "stage")
    private String stage;

    @Column(name = "lead_score")
    private Integer score;

    @Column(name = "lead_temperature")
    private String temperature;

    @Column(name = "source")
    private String source;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "role_code")
    private String roleCode;

    @Column(name = "branch_code")
    private String branchCode;

    @Column(name = "channel_code")
    private String channelCode;

    @Column(name = "product_type")
    private String productType;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @CreatedBy
    protected String createdBy= "customer";

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date lastModifiedAt;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id", referencedColumnName = "address_id")
    private AddressDetailsEntity addressDetailsEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    @JsonBackReference
    private Venue venue;



}
