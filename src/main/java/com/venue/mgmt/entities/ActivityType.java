package com.venue.mgmt.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "activitytypemaster", schema = "venuemgmt")
public class ActivityType {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "activity_type_seq")
    @SequenceGenerator(name = "activity_type_seq", sequenceName = "venuemgmt.activitytypemaster_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "activitytype", nullable = false)
    private String activityTypeName;

    @Column(name = "isactive")
    private Boolean isActive;

    @OneToMany(mappedBy = "activityType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Venue> venues;
}