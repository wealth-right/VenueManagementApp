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
    
    @Query(value = "SELECT l.* FROM lead_registration l " +
           "WHERE (:fullName IS NULL OR TRIM(:fullName) = '' OR l.full_name ILIKE CONCAT('%', TRIM(:fullName), '%')) " +
           "AND (:email IS NULL OR TRIM(:email) = '' OR l.email ILIKE CONCAT('%', TRIM(:email), '%')) " +
           "AND (:mobile IS NULL OR TRIM(:mobile) = '' OR l.mobile_number ILIKE CONCAT('%', TRIM(:mobile), '%')) " +
           "AND (l.is_active = true) " +
           "ORDER BY " +
           "CASE " +
           "    WHEN :sort = 'full_name' THEN l.full_name " +
           "    WHEN :sort = 'mobile_number' THEN l.mobile_number " +
           "    WHEN :sort = 'email' THEN l.email " +
           "    WHEN :sort = 'created_date' THEN CAST(l.created_date AS TEXT) " +
           "    ELSE l.full_name " +
           "END " +
           "CASE WHEN :direction = 'DESC' THEN ' DESC' ELSE ' ASC' END " +
           "LIMIT :pageSize OFFSET :offset", 
           nativeQuery = true)
    List<LeadRegistration> searchLeadsWithoutPaging(
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("mobile") String mobile,
            @Param("sort") String sort,
            @Param("direction") String direction,
            @Param("pageSize") int pageSize,
            @Param("offset") long offset);
    
    @Query(value = "SELECT COUNT(*) FROM lead_registration l " +
           "WHERE (:fullName IS NULL OR TRIM(:fullName) = '' OR l.full_name ILIKE CONCAT('%', TRIM(:fullName), '%')) " +
           "AND (:email IS NULL OR TRIM(:email) = '' OR l.email ILIKE CONCAT('%', TRIM(:email), '%')) " +
           "AND (:mobile IS NULL OR TRIM(:mobile) = '' OR l.mobile_number ILIKE CONCAT('%', TRIM(:mobile), '%')) " +
           "AND (l.is_active = true)",
           nativeQuery = true)
    long countSearchResults(
            @Param("fullName") String fullName,
            @Param("email") String email,
            @Param("mobile") String mobile);
}
