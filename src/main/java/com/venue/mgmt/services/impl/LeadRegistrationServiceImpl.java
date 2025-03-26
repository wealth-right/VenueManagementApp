package com.venue.mgmt.services.impl;

import com.venue.mgmt.dto.UserDetailsResponse;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.request.CustomerRequest;
import com.venue.mgmt.request.CustomerServiceClient;
import com.venue.mgmt.services.LeadRegistrationService;
import com.venue.mgmt.services.UserMgmtResService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.venue.mgmt.constant.GeneralMsgConstants.USER_ID;

@Service
@Slf4j
public class LeadRegistrationServiceImpl implements LeadRegistrationService {

    private static final Logger logger = LogManager.getLogger(LeadRegistrationServiceImpl.class);
    
    private final LeadRegRepository leadRegRepository;

    private final VenueRepository venueRepository;
    
    private final UserMgmtResService userMgmtResService;


    private final HttpServletRequest request;

    public LeadRegistrationServiceImpl(LeadRegRepository leadRegRepository, VenueRepository venueRepository,UserMgmtResService userMgmtResService,HttpServletRequest request) {
        this.leadRegRepository = leadRegRepository;
        this.venueRepository = venueRepository;
        this.userMgmtResService = userMgmtResService;
        this.request = request;

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
    public Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRange(String sortDirection, int page,
                                                                                                    int size, String userId, Long venueId,
                                                                                                    Date startDate, Date endDate) {
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
            String userId = request.getAttribute(USER_ID).toString();
            persistCustomerDetails(userId,existingLead.getCustomerId(), updatedLead);
            // Update the fields
            existingLead.setFullName(updatedLead.getFullName());
            existingLead.setEmail(updatedLead.getEmail());
            existingLead.setMobileNumber(updatedLead.getMobileNumber());
            existingLead.setStatus(updatedLead.getStatus());
            existingLead.setActive(true);
            existingLead.setLastModifiedBy(updatedLead.getLastModifiedBy());
            existingLead.setLastModifiedDate(updatedLead.getLastModifiedDate());
            existingLead.setMaritalStatus(updatedLead.getMaritalStatus());
            existingLead.setAge(updatedLead.getAge());
            existingLead.setOccupation(updatedLead.getOccupation());
            existingLead.setIncomeRange(updatedLead.getIncomeRange());
            existingLead.setDob(updatedLead.getDob());
            existingLead.setGender(updatedLead.getGender());
            existingLead.setPinCode(updatedLead.getPinCode());
            existingLead.setAddress(updatedLead.getAddress());
            existingLead.setLineOfBusiness(updatedLead.getLineOfBusiness());
            existingLead.setLifeStage(updatedLead.getLifeStage());
            existingLead.setVenue(updatedLead.getVenue());
            existingLead.setActive(updatedLead.getActive());
            existingLead.setRemarks(updatedLead.getRemarks());
            existingLead.setExistingProducts(updatedLead.getExistingProducts());


            LeadRegistration savedLead = leadRegRepository.save(existingLead);
            logger.info("Updated lead with ID: {}", savedLead.getLeadId());
            return savedLead;
        } catch (Exception e) {
            logger.error("Error while updating lead: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void persistCustomerDetails(String userId, String customerId,
                                        LeadRegistration leadRegistration) {
        // Fetch user details from the API
        CustomerServiceClient custServiceClient = new CustomerServiceClient(new RestTemplate());
        UserDetailsResponse.UserDetails userDetails = custServiceClient.getUserDetails(userId);
        if (userDetails == null) {
            return;
        }
        CustomerRequest customerRequest = userMgmtResService.getCustomerDetails(customerId);
        if (customerRequest == null) {
            logger.error("Customer not found with ID: {}", customerId);
            return;
        }
        // Create CustomerRequest object
        if ((!leadRegistration.getFullName().isEmpty()) && leadRegistration.getFullName() != null) {
            customerRequest.setFirstname(leadRegistration.getFullName().split(" ")[0]);
            customerRequest.setMiddlename(leadRegistration.getFullName().split(" ").length > 2 ? leadRegistration.getFullName().split(" ")[1] : "");
            customerRequest.setLastname(leadRegistration.getFullName().split(" ").length > 1 ? leadRegistration.getFullName().split(" ")[leadRegistration.getFullName().split(" ").length - 1] : "");
        }
        customerRequest.setFullname(leadRegistration.getFullName());
        customerRequest.setEmailid(leadRegistration.getEmail());
        customerRequest.setCountrycode("+91");
        customerRequest.setMobileno(leadRegistration.getMobileNumber());
        customerRequest.setAddedUpdatedBy(userId);
        customerRequest.setAssignedto(userId);
        if (leadRegistration.getGender() != null && (!leadRegistration.getGender().isEmpty())) {
            customerRequest.setGender(leadRegistration.getGender().substring(0, 1).toLowerCase());
            if (leadRegistration.getGender().equalsIgnoreCase("Male")) {
                customerRequest.setTitle("Mr.");
            } else if (leadRegistration.getGender().equalsIgnoreCase("Female") &&
                    leadRegistration.getMaritalStatus() != null
                    && (!leadRegistration.getMaritalStatus().isEmpty())
                    && leadRegistration.getMaritalStatus().equalsIgnoreCase("Married")) {
                customerRequest.setTitle("Mrs.");
            } else {
                customerRequest.setTitle("Miss.");
            }
        }
        customerRequest.setOccupation("01");
        customerRequest.setTaxStatus("01");
        customerRequest.setCountryOfResidence("India");
        customerRequest.setSource("QuickTapApp");
        customerRequest.setCustomertype("Prospect");
        customerRequest.setChannelcode(userDetails.getChannelcode());
        customerRequest.setBranchCode(userDetails.getBranchCode());
        // Save customer data
        CustomerServiceClient customerServiceClient = new CustomerServiceClient(new RestTemplate());
        customerServiceClient.saveCustomerData(customerRequest);
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
