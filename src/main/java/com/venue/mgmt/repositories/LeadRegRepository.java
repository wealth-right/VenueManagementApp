package com.venue.mgmt.repositories;

import com.venue.mgmt.entities.LeadRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRegRepository extends JpaRepository<LeadRegistration, Long> {
    Optional<LeadRegistration> findByFullName(String fullName);

    Optional<LeadRegistration> findByLeadId(Long leadId);
    
    @Query(value = "SELECT * FROM lead_registration l " +
           "WHERE l.is_active = true " +
           "AND (:searchTerm IS NULL OR TRIM(:searchTerm) = '' OR " +
           "     l.full_name ILIKE CONCAT('%', TRIM(:searchTerm), '%') OR " +
           "     l.email ILIKE CONCAT('%', TRIM(:searchTerm), '%') OR " +
           "     l.mobile_number ILIKE CONCAT('%', TRIM(:searchTerm), '%')) " +
           "ORDER BY l.creation_date DESC", 
           nativeQuery = true)
    List<LeadRegistration> searchLeads(@Param("searchTerm") String searchTerm);
}
