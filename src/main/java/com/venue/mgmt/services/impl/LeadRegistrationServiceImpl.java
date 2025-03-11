package com.venue.mgmt.services.impl;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.repositories.CampaignRepository;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.services.LeadRegistrationService;
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
public class LeadRegistrationServiceImpl implements LeadRegistrationService {

    private static final Logger logger = LogManager.getLogger(LeadRegistrationServiceImpl.class);
    
    @Autowired
    private LeadRegRepository leadRegRepository;

    @Autowired
    private VenueRepository venueRepository;

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
            LeadRegistration savedLead = leadRegRepository.save(leadRegistration);

            return savedLead;
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
    @Transactional(readOnly = true)
    public LeadRegistration getLeadById(Long leadId) {
        try {
            return leadRegRepository.findById(leadId)
                .orElseThrow(() -> new RuntimeException("Lead not found with id: " + leadId));
        } catch (Exception e) {
            logger.error("Error while fetching lead by id: {}", e.getMessage(), e);
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

            // Handle campaign update if provided
//            String campaignValue = updatedLead.getCampaign();
//            if (campaignValue != null && !campaignValue.trim().isEmpty()) {
//                Campaign campaign = existingLead.getCampaignEntity();
//                if (campaign == null) {
//                    campaign = new Campaign();
//                    campaign.setCampaignName(campaignValue);
//                    existingLead.addCampaign(campaign);
//                } else {
//                    campaign.setCampaignName(campaignValue);
//                }
//            }

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
