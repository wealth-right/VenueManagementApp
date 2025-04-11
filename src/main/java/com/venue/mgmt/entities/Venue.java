package com.venue.mgmt.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@ToString
@Entity
@Table(name = "venue", schema = "venuemgmt")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Venue extends Auditable<String> {

    private static final Logger logger = LogManager.getLogger(Venue.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venue_seq")
    @SequenceGenerator(name = "venue_seq", sequenceName = "venuemgmt.venue_id_seq", allocationSize = 1)
    @Column(name = "venue_id")
    Long venueId;

    @NotBlank(message = "Venue name is required")
    @Column(name = "venue_name", nullable = false)
    String venueName;

    @NotNull(message = "Latitude is required")
    @Column(name = "latitude")
    Double latitude;//non-mandatory

    @NotNull(message = "Longitude is required")
    @Column(name = "longitude")
    Double longitude;//non-mandatory

    @Column(name = "is_active")
    private Boolean isActive;

    @NotBlank(message = "Address is required")
    @Column(name = "address", nullable = false)
    String address;

    @Column(name = "locality")
    String locality;

    @Column(name = "city")
    String city;

    @Column(name = "state")
    String state;

    @Column(name = "country")
    String country;

    @Column(name = "pincode")
    String pinCode;
    //use the vector datatype instead of string
    //add the pincode as a new column

    @Transient
    private int leadCount;


    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    List<LeadRegistration> leads = new ArrayList<>();

    @Transient
    private int leadCountToday;

    public int getLeadCountToday() {
        LocalDate today = LocalDate.now();
        return (int) leads.stream()
                .filter(lead -> {
                    Date creationDate = lead.getCreationDate();
                    LocalDate creationLocalDate = Instant.ofEpochMilli(creationDate.getTime())
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    return creationLocalDate.equals(today);
                })
                .count();
    }

    public void setLeadCountToday(int leadCountToday) {
        this.leadCountToday = leadCountToday;
    }

    public void addLead(LeadRegistration lead) {
        leads.add(lead);
        lead.setVenue(this);
    }

    public void removeLead(LeadRegistration lead) {
        leads.remove(lead);
        lead.setVenue(null);
    }


    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        this.isActive = active;
    }

    @PrePersist
    public void logNewVenue() {
        logger.info("Creating new venue: {}", this.venueName);
    }
}
