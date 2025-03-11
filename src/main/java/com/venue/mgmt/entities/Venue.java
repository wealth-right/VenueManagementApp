package com.venue.mgmt.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "venue", schema = "public")
public class Venue extends Auditable<String> {

    private static final Logger logger = LogManager.getLogger(Venue.class);

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "venue_seq")
    @SequenceGenerator(name = "venue_seq", sequenceName = "venue_id_seq", allocationSize = 1)
    @Column(name = "venue_id")
    Long venueId;

    @NotBlank(message = "Venue name is required")
    @Column(name = "venue_name", nullable = false)
    String venueName;

    @NotNull(message = "Latitude is required")
    @Column(name = "latitude", nullable = false)
    Double latitude;

    @NotNull(message = "Longitude is required")
    @Column(name = "longitude", nullable = false)
    Double longitude;

    @Column(name = "is_active")
    private Boolean isActive;

    @NotBlank(message = "Address is required")
    @Column(name = "address", nullable = false)
    String address;
    //use the vector datatype instead of string
    //add the pincode as a new column

    @Transient
    private int leadCount;


    @OneToMany(mappedBy = "venue", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    @JsonIgnore
    List<LeadRegistration> leads = new ArrayList<>();

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
