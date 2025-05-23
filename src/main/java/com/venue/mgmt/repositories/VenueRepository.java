package com.venue.mgmt.repositories;

import com.venue.mgmt.entities.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    Optional<Venue> findByVenueId(Long venueId);

    @Query(value = "SELECT v.* FROM leadmgmt.venue v " +
            "JOIN usermgmt.usermaster m ON v.created_by = m.user_id " +
            "WHERE m.channelcode = :channelCode ",
            nativeQuery = true)
    Page<Venue> findByChannelCode(@Param("channelCode") String channelCode, Pageable pageable);
    
    @Query(value = "SELECT v.*, COUNT(l.lead_id) as lead_count FROM leadmgmt.venue v " +
            "LEFT JOIN leadmgmt.lead_details l ON v.venue_id = l.venue_id " +
            "WHERE v.is_active = true " +
            "AND (:searchTerm IS NULL OR :searchTerm = '' OR " +
            "     LOWER(v.venue_name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(v.address) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(v.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(v.pincode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(v.state) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "     LOWER(v.country) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
            "GROUP BY v.venue_id " +
            "ORDER BY v.creation_date DESC",
            nativeQuery = true)
    List<Venue> searchVenues(@Param("searchTerm") String searchTerm);



    @Query(value = "SELECT *, " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(l.latitude)) * " +
            "cos(radians(l.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(l.latitude)))) AS distance " +
            "FROM venue l " +
            "ORDER BY l.creation_date DESC",
            countQuery = "SELECT count(*) FROM venue l",
            nativeQuery = true)
    Page<Venue> findNearestLocations(@Param("lat") double latitude,
                                     @Param("lng") double longitude,
                                     Pageable pageable);


    List<Venue> findAllByCreatedBy(String createdBy);

    boolean existsByLatitudeAndLongitude(Double latitude, Double longitude);

    @Query(value = "SELECT v.* FROM leadmgmt.venue v " +
            "JOIN usermgmt.usermaster m ON v.created_by = m.user_id " +
            "WHERE m.channelcode = :channelCode ",
            nativeQuery = true)
    List<Venue> findByChannelCode(@Param("channelCode") String channelCode);
}
