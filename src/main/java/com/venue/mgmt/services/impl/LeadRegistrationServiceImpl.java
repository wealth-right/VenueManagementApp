package com.venue.mgmt.services.impl;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.exception.EmailAlreadyExistException;
import com.venue.mgmt.exception.LeadNotFoundException;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.request.CustomerRequest;
import com.venue.mgmt.request.CustomerServiceClient;
import com.venue.mgmt.request.UserMasterRequest;
import com.venue.mgmt.services.LeadRegistrationService;
import com.venue.mgmt.services.UserMgmtResService;
import com.venue.mgmt.services.impl.utils.OccupationCodesUtil;
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

import static com.venue.mgmt.constant.GeneralMsgConstants.USER_ID;

@Service
@Slf4j
public class LeadRegistrationServiceImpl implements LeadRegistrationService {

    private static final Logger logger = LogManager.getLogger(LeadRegistrationServiceImpl.class);

    private static final String COUNTRY="India";

    private static final String SOURCE = "QuickTapApp";

    private static final String TYPE = "Prospect";

    private final LeadRegRepository leadRegRepository;

    private final VenueRepository venueRepository;

    private final UserMgmtResService userMgmtResService;



    private final HttpServletRequest request;

    public LeadRegistrationServiceImpl(LeadRegRepository leadRegRepository, VenueRepository venueRepository, UserMgmtResService userMgmtResService, HttpServletRequest request) {
        this.leadRegRepository = leadRegRepository;
        this.venueRepository = venueRepository;
        this.userMgmtResService = userMgmtResService;
        this.request = request;
    }

    @Override
    @Transactional
    public LeadRegistration saveLead(LeadRegistration leadRegistration) {
        Venue venue = venueRepository.findByVenueId(leadRegistration.getVenue().getVenueId())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + leadRegistration.getVenue().getVenueId()));
        if (leadRegistration.getEmail() != null && !leadRegistration.getEmail().trim().isEmpty()) {
            if (leadRegRepository.findByEmail(leadRegistration.getEmail()).isPresent()) {
                throw new EmailAlreadyExistException("Lead with email " + leadRegistration.getEmail() + " already exists.");
            }else{
                leadRegistration.setEmail(leadRegistration.getEmail());
            }
        }else{
            logger.warn("Email is null or empty for lead registration. Skipping email uniqueness check.");
            leadRegistration.setEmail(null); // Ensure email is set to null if not provided
        }
        if ((!leadRegistration.getFullName().isEmpty()) && leadRegistration.getFullName() != null) {
            leadRegistration.setFirstName(leadRegistration.getFullName().split(" ")[0]);
            leadRegistration.setMiddleName(leadRegistration.getFullName().split(" ").length > 2 ? leadRegistration.getFullName().split(" ")[1] : "");
            leadRegistration.setLastName(leadRegistration.getFullName().split(" ").length > 1 ? leadRegistration.getFullName().split(" ")[leadRegistration.getFullName().split(" ").length - 1] : "");
        }
        leadRegistration.setPhoneNumber(leadRegistration.getMobileNumber());
        logger.info("Starting to save lead with Venue Name: {}", venue.getVenueName());
        // Save the lead registration
        leadRegistration.setVenue(venue);
        logger.info("Saving lead registration...");
        return leadRegRepository.save(leadRegistration);
    }


    @Override
    @Transactional(readOnly = true)
    public Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedByAndIsDeletedFalse(String sortDirection, int page, int size, String userId) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);
        return leadRegRepository.findAllByUserIdAndIsDeletedFalse(userId, pageable);
    }

    @Override
    public Page<LeadRegistration> getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRangeAndIsDeletedFalse(String sortDirection, int page,
                                                                                                                     int size, String userId, Long venueId,
                                                                                                                     Date startDate, Date endDate) {
        Sort.Direction direction = sortDirection.contains("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, "creationDate");
        Pageable pageable = PageRequest.of(page, size, sort);

        if (venueId != null) {
            if (startDate != null && endDate != null) {
                return leadRegRepository.findAllByUserIdAndVenueIdAndCreationDateBetweenAndIsDeletedFalse(userId, venueId, startDate, endDate, pageable);
            } else if (startDate != null) {
                return leadRegRepository.findAllByUserIdAndVenueIdAndCreationDateAfterAndIsDeletedFalse(userId, venueId, startDate, pageable);
            } else if (endDate != null) {
                return leadRegRepository.findAllByUserIdAndVenueIdAndCreationDateBeforeAndIsDeletedFalse(userId, venueId, endDate, pageable);
            } else {
                return leadRegRepository.findAllByUserIdAndVenueIdAndIsDeletedFalse(userId, venueId, pageable);
            }
        } else {
            if (startDate != null && endDate != null) {
                return leadRegRepository.findAllByUserIdAndCreationDateBetweenAndIsDeletedFalse(userId, startDate, endDate, pageable);
            } else if (startDate != null) {
                return leadRegRepository.findAllByUserIdAndCreationDateAfterAndIsDeletedFalse(userId, startDate, pageable);
            } else if (endDate != null) {
                return leadRegRepository.findAllByUserIdAndCreationDateBeforeAndIsDeletedFalse(userId, endDate, pageable);
            } else {
                return leadRegRepository.findAllByUserIdAndIsDeletedFalse(userId, pageable);
            }
        }
    }


    @Override
    @Transactional(readOnly = true)
    public List<LeadRegistration> simpleSearchLeads(String searchTerm, String userId) {

        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllLeadsSortedByCreationDateAndCreatedByAndIsDeletedFalse("desc", 0, Integer.MAX_VALUE, userId).getContent();
        }
        return leadRegRepository.searchLeads(searchTerm, userId);
    }

    @Override
    @Transactional
    public LeadRegistration updateLead(Long leadId, LeadRegistration updatedLead, String authHeader) {
        LeadRegistration existingLead = leadRegRepository.findById(leadId)
                .orElseThrow(() -> new LeadNotFoundException("Lead not found with id: " + leadId));
        String userId = request.getAttribute(USER_ID).toString();
        persistCustomerDetails(userId, existingLead.getCustomerId(), updatedLead, authHeader);
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
    }

    public String persistCustomerDetails(String userId, LeadRegistration leadRegistration,String authHeader) {
        logger.info("VenueManagementApp - Inside persistCustomerDetails Method");
        UserMasterRequest userMasterDetails = userMgmtResService.getUserMasterDetails(userId);
        if(userMasterDetails == null){
            return null;
        }
        CustomerRequest customerRequest = new CustomerRequest();
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
        if (leadRegistration.getGender() != null && (!leadRegistration.getGender().isEmpty())) {
            customerRequest.setGender(leadRegistration.getGender().substring(0, 1).toLowerCase());
            if (leadRegistration.getGender().equalsIgnoreCase("Male")) {
                customerRequest.setTitle("Mr.");
            } else if (leadRegistration.getGender().equalsIgnoreCase("Female") && leadRegistration.getMaritalStatus() != null
                    && (!leadRegistration.getMaritalStatus().isEmpty())
                    && leadRegistration.getMaritalStatus().equalsIgnoreCase("Married")) {
                customerRequest.setTitle("Mrs.");
            } else {
                customerRequest.setTitle("Miss.");
            }
        }
        String occupation=null;
        if(leadRegistration.getOccupation()!=null && (!leadRegistration.getOccupation().isEmpty())){
            occupation = OccupationCodesUtil.mapOccupationToCode(leadRegistration.getOccupation());
        }
        customerRequest.setOccupation(occupation);
        customerRequest.setTaxStatus("01");
        customerRequest.setCountryOfResidence(COUNTRY);
        customerRequest.setSource(SOURCE);
        customerRequest.setCustomertype(TYPE);
        customerRequest.setChannelcode(userMasterDetails.getChannelCode());
        customerRequest.setBranchCode(userMasterDetails.getBranchCode());
        CustomerServiceClient customerServiceClient = new CustomerServiceClient(new RestTemplate());
        ResponseEntity<String> entity = customerServiceClient.saveCustomerData(customerRequest,authHeader);
        return entity.getBody();
    }

    private void persistCustomerDetails(String userId, String customerId,
                                        LeadRegistration leadRegistration, String authHeader) {
        UserMasterRequest userMasterDetails = userMgmtResService.getUserMasterDetails(userId);
        if (userMasterDetails == null) {
            return;
        }
        CustomerRequest customerRequest = userMgmtResService.getCustomerDetails(customerId);
        CustomerRequest custRequest = new CustomerRequest();
        if (customerRequest == null) {
            logger.error("Customer not found with ID: {}", customerId);
            return;
        }
        if ((!leadRegistration.getFullName().isEmpty()) && leadRegistration.getFullName() != null) {
            custRequest.setFirstname(leadRegistration.getFullName().split(" ")[0]);
            custRequest.setMiddlename(leadRegistration.getFullName().split(" ").length > 2 ? leadRegistration.getFullName().split(" ")[1] : "");
            custRequest.setLastname(leadRegistration.getFullName().split(" ").length > 1 ? leadRegistration.getFullName().split(" ")[leadRegistration.getFullName().split(" ").length - 1] : "");
        }
        custRequest.setFullname(leadRegistration.getFullName());
        custRequest.setMobileno(leadRegistration.getMobileNumber());
        custRequest.setEmailid(leadRegistration.getEmail());
        custRequest.setCountrycode("+91");
        custRequest.setCustomerId(customerId);
        custRequest.setAddedUpdatedBy(userId);
        if (leadRegistration.getGender() != null && (!leadRegistration.getGender().isEmpty())) {
            custRequest.setGender(leadRegistration.getGender().substring(0, 1).toLowerCase());
            if (leadRegistration.getGender().equalsIgnoreCase("Male")) {
                custRequest.setTitle("Mr.");
            } else if (leadRegistration.getGender().equalsIgnoreCase("Female") &&
                    leadRegistration.getMaritalStatus() != null && (!leadRegistration.getMaritalStatus().isEmpty()) && leadRegistration.getMaritalStatus().equalsIgnoreCase("Married")) {
                custRequest.setTitle("Mrs.");
            } else {
                custRequest.setTitle("Miss.");
            }
        }
        String occupation = null;
        if(leadRegistration.getOccupation()!=null && (!leadRegistration.getOccupation().isEmpty())){
            occupation=OccupationCodesUtil.mapOccupationToCode(leadRegistration.getOccupation());
        }
        custRequest.setOccupation(occupation);
        custRequest.setTaxStatus("01");
        custRequest.setCountryOfResidence(COUNTRY);
        custRequest.setSource(SOURCE);
        custRequest.setCustomertype(TYPE);
        custRequest.setChannelcode(userMasterDetails.getChannelCode());
        custRequest.setBranchCode(userMasterDetails.getBranchCode());
        CustomerServiceClient customerServiceClient = new CustomerServiceClient(new RestTemplate());
        customerServiceClient.saveCustomerData(custRequest, authHeader);
    }

    @Override
    @Transactional
    public void deleteLead(Long leadId,String authHeader) {
        LeadRegistration lead = leadRegRepository.findById(leadId)
                .orElseThrow(() -> new LeadNotFoundException("Lead not found with id: " + leadId));
        CustomerServiceClient customerServiceClient = new CustomerServiceClient(new RestTemplate());
        String customerId = lead.getCustomerId();
        customerServiceClient.deleteCustomer(customerId,authHeader);
        lead.setDeleted(true);
        lead.setActive(false);
        leadRegRepository.save(lead);
        logger.info("Marked lead with ID: {} as deleted", leadId);
    }

}
