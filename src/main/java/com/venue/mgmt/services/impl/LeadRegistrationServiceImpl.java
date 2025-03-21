package com.venue.mgmt.services.impl;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.services.LeadRegistrationService;
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

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class LeadRegistrationServiceImpl implements LeadRegistrationService {

    private static final Logger logger = LogManager.getLogger(LeadRegistrationServiceImpl.class);
    
    private final LeadRegRepository leadRegRepository;

    private final VenueRepository venueRepository;

    public LeadRegistrationServiceImpl(LeadRegRepository leadRegRepository, VenueRepository venueRepository) {
        this.leadRegRepository = leadRegRepository;
        this.venueRepository = venueRepository;

    }

    @Override
    @Transactional
    public LeadRegistration saveLead(LeadRegistration leadRegistration) {
        try {
            Venue venue = venueRepository.findByVenueId(leadRegistration.getVenue().getVenueId())
                    .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + leadRegistration.getVenue().getVenueId()));
            logger.info("Starting to save lead with Venue Name: {}", venue.getVenueName());
            // Save the lead registration
            leadRegistration.setVenue(venue);
            logger.info("Saving lead registration...");
            return leadRegRepository.save(leadRegistration);
        } catch (Exception e) {
            logger.error("Error while saving lead with campaign: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedBy(String sortDirection, int page, int size, String userId) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, "creationDate");
            Pageable pageable = PageRequest.of(page, size, sort);
            return leadRegRepository.findAllByUserId(userId,pageable);
        } catch (Exception e) {
            logger.error("Error while fetching all leads: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRange(String sortDirection, int page, int size, String userId, Long venueId, Date startDate, Date endDate) {
        try {
            Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                    Sort.Direction.DESC : Sort.Direction.ASC;
            Sort sort = Sort.by(direction, "creationDate");
            Pageable pageable = PageRequest.of(page, size, sort);
            if (startDate != null && endDate != null) {
                return leadRegRepository.findAllByUserIdAndVenueIdAndCreationDateBetween(userId, venueId, startDate, endDate, pageable);
            } else if (startDate != null) {
                return leadRegRepository.findAllByUserIdAndVenueIdAndCreationDateAfter(userId, venueId, startDate, pageable);
            } else if (endDate != null) {
                return leadRegRepository.findAllByUserIdAndVenueIdAndCreationDateBefore(userId, venueId, endDate, pageable);
            } else if (venueId!=null){
                return leadRegRepository.findAllByUserIdAndVenueId(userId, venueId, pageable);
            }else{
                return leadRegRepository.findAllByUserId(userId, pageable);
            }
        } catch (Exception e) {
            logger.error("Error while fetching all leads: {}", e.getMessage(), e);
            throw e;
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<LeadRegistration> simpleSearchLeads(String searchTerm,String userId) {

        try {
            if (searchTerm == null || searchTerm.trim().isEmpty()) {
                return getAllLeadsSortedByCreationDateAndCreatedBy("desc", 0, Integer.MAX_VALUE,userId).getContent();
            }
            return leadRegRepository.searchLeads(searchTerm,userId);
        } catch (Exception e) {
            logger.error("Error while searching leads: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public LeadRegistration updateLead(Long leadId, LeadRegistration updatedLead) {
        try {
            LeadRegistration existingLead = leadRegRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + leadId));

            // Update the fields
            existingLead.setEmail(updatedLead.getEmail());
            existingLead.setMobileNumber(updatedLead.getMobileNumber());
            existingLead.setStatus(updatedLead.getStatus());
            existingLead.setFullName(updatedLead.getFullName());
            existingLead.setActive(true);
            existingLead.setLastModifiedBy(updatedLead.getLastModifiedBy());
            existingLead.setLastModifiedDate(updatedLead.getLastModifiedDate());
            existingLead.setMaritalStatus(updatedLead.getMaritalStatus());
            existingLead.setAge(updatedLead.getAge());
            existingLead.setFullName(updatedLead.getFullName());
            existingLead.setOccupation(updatedLead.getOccupation());
            existingLead.setIncomeRange(updatedLead.getIncomeRange());
            existingLead.setDob(updatedLead.getDob());
            existingLead.setGender(updatedLead.getGender());
            existingLead.setAddress(updatedLead.getAddress());
            existingLead.setExistingProducts(updatedLead.getExistingProducts());


            LeadRegistration savedLead = leadRegRepository.save(existingLead);
            logger.info("Updated lead with ID: {}", savedLead.getLeadId());
            return savedLead;
        } catch (Exception e) {
            logger.error("Error while updating lead: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteLead(Long leadId) {
        try {
            LeadRegistration lead = leadRegRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + leadId));
            

            leadRegRepository.delete(lead);
            logger.info("Deleted lead with ID: {}", leadId);
        } catch (Exception e) {
            logger.error("Error while deleting lead: {}", e.getMessage(), e);
            throw e;
        }
    }

}
