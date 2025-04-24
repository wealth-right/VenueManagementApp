package com.venue.mgmt.services.impl;

import com.venue.mgmt.constant.ErrorMsgConstants;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.exception.VenueAlreadyExistsException;
import com.venue.mgmt.exception.VenueNotSavedException;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.services.VenueService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

@Service
@Slf4j
public class VenueServiceImpl implements VenueService {

    private static final Logger logger = LogManager.getLogger(VenueServiceImpl.class);

    private final VenueRepository venueRepository;

    private final LeadRegRepository leadRegRepository;

    private final VenueRepository venueRepos;


    public VenueServiceImpl(VenueRepository venueRepository, LeadRegRepository leadRegRepository,VenueRepository venueRepos) {
        this.venueRepository = venueRepository;
        this.leadRegRepository = leadRegRepository;
        this.venueRepos = venueRepos;
    }

    @Override
    @Transactional
    public Venue saveVenue(Venue venue) throws VenueAlreadyExistsException {
        try {
            logger.info("Saving new venue: {}", venue.getVenueName());
            venue.setIsActive(true);
            if (venue.getLatitude() != null && venue.getLongitude() != null) {
                boolean venueExists = venueRepos.existsByLatitudeAndLongitude(venue.getLatitude(), venue.getLongitude());
                if (venueExists) {
                    throw new VenueAlreadyExistsException("Venue with the same latitude "+venue.getLatitude()+" and longitude "+venue.getLongitude() +" already exists.");
                }
            }
            return venueRepository.save(venue);
        }
        catch(VenueAlreadyExistsException e){
            logger.error("Venue already exists: {}", e.getMessage());
            throw e;
        }
        catch (Exception e) {
            logger.error("Error saving venue: {}", e.getMessage());
            throw new VenueNotSavedException("Failed to save venue " + venue.getVenueName());
        }
    }

    @Override
    public List<Venue> getVenuesByIds(List<Long> venueIds) {
        return venueRepository.findAllById(venueIds);
    }


    @Override
    public List<Venue> searchVenues(String searchTerm, String userId) {
        try{
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllVenuesSortedByCreationDate("desc", 0, Integer.MAX_VALUE, userId).getContent();
            }
            return venueRepository.searchVenues(searchTerm);
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
                    venue.setActivityType(updatedVenue.getActivityType());
                    venue.setPinCode(updatedVenue.getPinCode());
                    venue.setLocality(updatedVenue.getLocality());
                    venue.setCity(updatedVenue.getCity());
                    venue.setState(updatedVenue.getState());
                    venue.setCountry(updatedVenue.getCountry());
                    return venueRepository.save(venue);
                })
                .orElseThrow(() -> new EntityNotFoundException(ErrorMsgConstants.VENUE_NOT_FOUND + venueId));
    }



    @Override
    @Transactional
    public void deleteVenue(Long venueId) {
        Venue venue = venueRepository.findByVenueId(venueId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMsgConstants.VENUE_NOT_FOUND + venueId));
        venue.setIsActive(false);
        venueRepository.save(venue);
    }

    @Override
    @Transactional
    public Venue addLeadToVenue(LeadRegistration leadRegistration) {
        Venue venue = venueRepository.findByVenueId(leadRegistration.getVenue().getVenueId())
                .orElseThrow(() -> new EntityNotFoundException(ErrorMsgConstants.VENUE_NOT_FOUND + leadRegistration.getVenue().getVenueId()));
        
        LeadRegistration lead = leadRegRepository.findByLeadId(leadRegistration.getLeadId())
                .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + leadRegistration.getLeadId()));

        venue.addLead(lead);
        return venueRepository.save(venue);
    }

    public List<Venue> findNearestVenues(double targetLat, double targetLon, int k, String userId) {
        List<Venue> allVenues = venueRepository.findAllByCreatedBy(userId);

        PriorityQueue<Venue> maxHeap = new PriorityQueue<>((a, b) -> {
            double distA = calculateDistance(targetLat, targetLon, a.getLatitude(), a.getLongitude());
            double distB = calculateDistance(targetLat, targetLon, b.getLatitude(), b.getLongitude());
            return Double.compare(distB, distA);
        });
        for (Venue venue : allVenues) {
            double distance = calculateDistance(targetLat, targetLon, venue.getLatitude(), venue.getLongitude());
            venue.setDistance(distance);
            maxHeap.offer(venue);
            if (maxHeap.size() > k) {
                maxHeap.poll();
            }
        }
        List<Venue> nearest = new ArrayList<>(maxHeap);
        nearest.sort(Comparator.comparingDouble(Venue::getDistance));
        return nearest;
    }

    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS_KM = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }


    public Page<Venue> getAllVenuesSortedByDistance(String sortDirection, Double latitude, Double longitude, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return venueRepository.findNearestLocations(latitude, longitude, pageable);
    }

    @Override
    public Page<Venue> getAllVenuesSortedByCreationDate(String sortDirection, int page, int size, String userId) {
        Sort.Direction direction = sortDirection.contains("DESC") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, "creationDate");
        Pageable pageable = PageRequest.of(page, size, sort);
        return venueRepository.findAll(pageable);
    }

}
