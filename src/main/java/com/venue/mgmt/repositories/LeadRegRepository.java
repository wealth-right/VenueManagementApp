package com.venue.mgmt.repositories;

import com.venue.mgmt.entities.LeadRegistration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeadRegRepository extends JpaRepository<LeadRegistration, Long> {

    Optional<LeadRegistration> findByLeadId(Long leadId);

    int countByVenue_VenueIdAndCreatedByAndIsDeletedFalse(Long venueId, String createdBy);

//    @Query(" select count(lr1_0.leadId)  from LeadRegistration lr1_0 left join " +
//            "Venue v1_0 on v1_0.venueId=lr1_0.venue.venueId where  v1_0.venueId=:venueId and lr1_0.createdBy=:createdBy and lr1_0.creationDate between :startDate and :endDate and lr1_0.isDeleted=false")
    int countByVenue_VenueIdAndCreatedByAndCreationDateAndIsDeletedFalse(Long venueId,String createdBy,Date startDate);

    @Query("SELECT l FROM LeadRegistration l WHERE l.createdBy = :userId and l.isDeleted = false")
    Page<LeadRegistration> findAllByUserIdAndIsDeletedFalse(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT l FROM LeadRegistration l WHERE l.createdBy = :userId and l.venue.venueId = :venueId and l.isDeleted = false")
    Page<LeadRegistration> findAllByUserIdAndVenueIdAndIsDeletedFalse(@Param("userId") String userId, @Param("venueId") Long venueId, Pageable pageable);

    @Query("SELECT l FROM LeadRegistration l WHERE l.createdBy = :userId  and l.creationDate BETWEEN :startDate AND :endDate and l.isDeleted = false")
    Page<LeadRegistration> findAllByUserIdAndCreationDateBetweenAndIsDeletedFalse(@Param("userId") String userId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

    @Query("SELECT l FROM LeadRegistration l WHERE l.createdBy = :userId and l.venue.venueId = :venueId and l.creationDate BETWEEN :startDate AND :endDate and l.isDeleted = false")
    Page<LeadRegistration> findAllByUserIdAndVenueIdAndCreationDateBetweenAndIsDeletedFalse(@Param("userId") String userId, @Param("venueId") Long venueId, @Param("startDate") Date startDate, @Param("endDate") Date endDate, Pageable pageable);

    @Query("SELECT l FROM LeadRegistration l WHERE l.createdBy = :userId and l.creationDate >= :startDate and l.isDeleted = false")
    Page<LeadRegistration> findAllByUserIdAndCreationDateAfterAndIsDeletedFalse(@Param("userId") String userId, @Param("startDate") Date startDate, Pageable pageable);
    @Query("SELECT l FROM LeadRegistration l WHERE l.createdBy = :userId and l.venue.venueId = :venueId and l.creationDate >= :startDate and l.isDeleted = false")
    Page<LeadRegistration> findAllByUserIdAndVenueIdAndCreationDateAfterAndIsDeletedFalse(@Param("userId") String userId, @Param("venueId") Long venueId, @Param("startDate") Date startDate, Pageable pageable);

    @Query("SELECT l FROM LeadRegistration l WHERE l.createdBy = :userId and l.creationDate <= :endDate and l.isDeleted = false")
    Page<LeadRegistration> findAllByUserIdAndCreationDateBeforeAndIsDeletedFalse(@Param("userId") String userId,
                                                                                           @Param("endDate") Date endDate, Pageable pageable);


    @Query("SELECT l FROM LeadRegistration l WHERE l.createdBy = :userId and l.venue.venueId = :venueId and l.creationDate <= :endDate and l.isDeleted = false")
    Page<LeadRegistration> findAllByUserIdAndVenueIdAndCreationDateBeforeAndIsDeletedFalse(@Param("userId") String userId,
                                                                          @Param("venueId") Long venueId,
                                                                          @Param("endDate") Date endDate, Pageable pageable);
    
    @Query(value = "SELECT * FROM leadmgmt.lead_registration l " +
           "WHERE l.is_active = true " +
            "AND l.created_by=:userId "+
            "AND l.is_deleted = false " +
           "AND (:searchTerm IS NULL OR TRIM(:searchTerm) = '' OR " +
           "     l.full_name ILIKE CONCAT('%', TRIM(:searchTerm), '%') OR " +
           "     l.email ILIKE CONCAT('%', TRIM(:searchTerm), '%') OR " +
           "     l.mobile_number ILIKE CONCAT('%', TRIM(:searchTerm), '%')" +
            ") " +
           "ORDER BY l.creation_date DESC", 
           nativeQuery = true)
    List<LeadRegistration> searchLeads(@Param("searchTerm") String searchTerm, @Param("userId") String userId);

    @Query("select l from LeadRegistration l where l.venue.venueId=:venueId  " +
            "and l.createdBy=:userId")
    List<LeadRegistration> findByVenueIdAndCreated_by(@Param("venueId") Long venueId, @Param("userId") String userId);

    @Query("select l from LeadRegistration l where l.venue.venueId=:venueId and l.isDeleted=false and l.createdBy=:userId")
    List<LeadRegistration> findByVenueIdAndIsDeletedFalseAndCreatedBy(@Param("venueId") Long venueId, @Param("userId") String userId);
}
