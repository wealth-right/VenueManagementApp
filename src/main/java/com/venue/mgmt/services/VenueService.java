package com.venue.mgmt.services;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import org.springframework.data.domain.Page;

import java.util.List;

public interface VenueService {
    Venue saveVenue(Venue venue);

    List<Venue> getVenuesByIds(List<Long> venueIds);
    Page<Venue> getAllVenuesSortedByCreationDate(String sortDirection, int page, int size, String userId);
    //sort by nearest as well by passing the longitude and latitude
    List<Venue> searchVenues(String searchTerm, String userId);
    Venue updateVenue(Long venueId, Venue venue);
    void deleteVenue(Long venueId);
    Venue addLeadToVenue(LeadRegistration leadRegistration);

    Page<Venue> getAllVenuesSortedByDistance(String sortDirection, Double latitude, Double longitude, int page, int size);
 }
