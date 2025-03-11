package com.venue.mgmt.services.impl;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.services.VenueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class VenueServiceImpl implements VenueService {

    private static final Logger logger = LogManager.getLogger(VenueServiceImpl.class);

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private LeadRegRepository leadRegRepository;

    @Override
    @Transactional
    public Venue saveVenue(Venue venue) {
        try {
            logger.info("Saving new venue: {}", venue.getVenueName());
            venue.setIsActive(true);
            venue.setLeads(null);
            return venueRepository.save(venue);
        } catch (Exception e) {
            logger.error("Error saving venue: {}", e.getMessage());
            throw new RuntimeException("Failed to save venue", e);
        }
    }


    @Override
    public List<Venue> searchVenues(String searchTerm, String userId) {
        try{
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllVenuesSortedByCreationDate("desc", 0, Integer.MAX_VALUE, userId).getContent();
            }
            return venueRepository.searchVenues(searchTerm,userId);
        } catch (Exception e) {
            logger.error("Error while searching venues: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Override
    @Transactional
    public Venue updateVenue(Long venueId, Venue updatedVenue) {
        return venueRepository.findByVenueId(venueId)
                .map(venue -> {
                    venue.setVenueName(updatedVenue.getVenueName());
                    venue.setLatitude(updatedVenue.getLatitude());
                    venue.setLongitude(updatedVenue.getLongitude());
                    venue.setAddress(updatedVenue.getAddress());
                    return venueRepository.save(venue);
                })
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + venueId));
    }

    @Override
    @Transactional
    public void deleteVenue(Long venueId) {
        Venue venue = venueRepository.findByVenueId(venueId)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + venueId));
        venue.setIsActive(false);
        venueRepository.save(venue);
    }

    @Override
    @Transactional
    public Venue addLeadToVenue(LeadRegistration leadRegistration) {
        Venue venue = venueRepository.findByVenueId(leadRegistration.getVenue().getVenueId())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + leadRegistration.getVenue().getVenueId()));
        
        LeadRegistration lead = leadRegRepository.findByLeadId(leadRegistration.getLeadId())
                .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + leadRegistration.getLeadId()));

        venue.addLead(lead);
        return venueRepository.save(venue);
    }

    @Override
    @Transactional
    public Venue removeLeadFromVenue(Long venueId, Long leadId) {
        Venue venue = venueRepository.findByVenueId(venueId)
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + venueId));
        
        LeadRegistration lead = leadRegRepository.findByLeadId(leadId)
                .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + leadId));

        venue.removeLead(lead);
        return venueRepository.save(venue);
    }

    @Override
    public Page<Venue> getAllVenuesSorted(String sortBy, String sortDirection, Double latitude, Double longitude, int page, int size) {
        return venueRepository.findAllVenueByDistance(latitude, longitude, sortDirection, PageRequest.of(page, size));
    }

    @Override
    public Page<Venue> getAllVenuesSortedByCreationDate(String sortDirection, int page, int size, String userId) {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection.toUpperCase());
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, "creationDate"));
        return venueRepository.findAll(pageable);
    }

}
