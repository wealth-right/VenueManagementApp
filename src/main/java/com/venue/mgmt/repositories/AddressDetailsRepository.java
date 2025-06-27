package com.venue.mgmt.repositories;

import com.venue.mgmt.entities.AddressDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AddressDetailsRepository extends JpaRepository<AddressDetailsEntity, Long> {

    @Query("SELECT a FROM AddressDetailsEntity a WHERE a.leadDetailsEntity.id = :leadId")
    Optional<AddressDetailsEntity> findByLeadId(Long leadId);
}

