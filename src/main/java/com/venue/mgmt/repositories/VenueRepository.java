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

    @Query(value = "SELECT v.*, COUNT(l.lead_id) as lead_count FROM venue v " +
            "LEFT JOIN lead_registration l ON v.venue_id = l.venue_id " +
            "WHERE v.is_active = true " +
            "AND v.created_by = :userId " +
            "AND (:searchTerm IS NULL OR TRIM(:searchTerm) = '' OR " +
            "     v.venue_name ILIKE CONCAT('%', TRIM(:searchTerm), '%') OR " +
            "     v.address ILIKE CONCAT('%', TRIM(:searchTerm), '%')) " +
            "GROUP BY v.venue_id " +
            "ORDER BY v.creation_date DESC",
            nativeQuery = true)
    List<Venue> searchVenues(@Param("searchTerm") String searchTerm, @Param("userId") String userId);

    @Query(value = "SELECT * FROM venue " +
            "ORDER BY point(longitude, latitude) <-> point(:userLong, :userLat) " +
            ":sortDirection", nativeQuery = true)
    Page<Venue> findAllVenueByDistance(
            @Param("userLat") Double userLat,
            @Param("userLong") Double userLong,
            @Param("sortDirection") String sortDirection,
            Pageable pageable);

}
