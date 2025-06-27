package com.venue.mgmt.services.impl;

import com.venue.mgmt.dto.LeadScoringDTO;
import com.venue.mgmt.dto.LeadWithVenueDetails;
import com.venue.mgmt.entities.AddressDetailsEntity;
import com.venue.mgmt.entities.LeadDetailsEntity;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.exception.LeadNotFoundException;
import com.venue.mgmt.repositories.AddressDetailsRepository;
import com.venue.mgmt.repositories.LeadDetailsRepository;
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
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;


import static com.venue.mgmt.constant.GeneralMsgConstants.USER_ID;

@Service
@Slf4j
public class LeadRegistrationServiceImpl implements LeadRegistrationService {

    private static final Logger logger = LogManager.getLogger(LeadRegistrationServiceImpl.class);

    private static final String COUNTRY="India";

    private static final String SOURCE = "QuickTapApp";

    private static final String TYPE = "Prospect";
    private static final String NEW_STAGE = "NEW";
    private static final String LEAD_NOT_FOUND = "Lead not found with id: ";
    private static final String LEAD_SCORE_URL = "https://sit-services.wealth-right.com/api/lmsapi/api/v1/public/lead-score";

    private final LeadRegRepository leadRegRepository;

    private final LeadDetailsRepository leadDetailsRepository;

    private final VenueRepository venueRepository;

    private final UserMgmtResService userMgmtResService;

    private final AddressDetailsRepository addressDetailsRepository;
    private final RestTemplate restTemplate;



    private final HttpServletRequest request;

    public LeadRegistrationServiceImpl(LeadRegRepository leadRegRepository, VenueRepository venueRepository, UserMgmtResService userMgmtResService,
                                       HttpServletRequest request, LeadDetailsRepository leadDetailsRepository,
                                       AddressDetailsRepository  addressDetailsRepository,RestTemplate restTemplate) {
        this.leadRegRepository = leadRegRepository;
        this.venueRepository = venueRepository;
        this.userMgmtResService = userMgmtResService;
        this.request = request;
        this.leadDetailsRepository = leadDetailsRepository;
        this.addressDetailsRepository = addressDetailsRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public LeadRegistration saveLead(LeadRegistration leadRegistration) {
        Venue venue = venueRepository.findByVenueId(leadRegistration.getVenue().getVenueId())
                .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + leadRegistration.getVenue().getVenueId()));
        if ((!leadRegistration.getFullName().isEmpty()) && leadRegistration.getFullName() != null) {
            leadRegistration.setFirstName(leadRegistration.getFullName().split(" ")[0]);
            leadRegistration.setMiddleName(leadRegistration.getFullName().split(" ").length > 2 ? leadRegistration.getFullName().split(" ")[1] : "");
            leadRegistration.setLastName(leadRegistration.getFullName().split(" ").length > 1 ? leadRegistration.getFullName().split(" ")[leadRegistration.getFullName().split(" ").length - 1] : "");
        }
        leadRegistration.setPhoneNumber(leadRegistration.getMobileNumber());
        logger.info("Starting to save lead with Venue Name: {}", venue.getVenueName());
        // Save the lead registration
        leadRegistration.setVenue(venue);
        leadRegistration.setSource(SOURCE);
        leadRegistration.setStage(NEW_STAGE);
        LeadDetailsEntity leadEntity = convertToEntity(leadRegistration);
        leadEntity.setVenue(venue);
        AddressDetailsEntity address = addressMapping(leadRegistration);
        address.setLeadDetailsEntity(leadEntity); // bidirectional link
        leadEntity.setAddressDetailsEntity(address);
        logger.info("Saving lead registration...");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<LeadDetailsEntity> requestEntity = new HttpEntity<>(leadEntity, headers);

            ResponseEntity<LeadScoringDTO> response = restTemplate.exchange(
                    LEAD_SCORE_URL,
                    HttpMethod.POST,
                    requestEntity,
                    LeadScoringDTO.class
            );
            if (response.getStatusCode().is2xxSuccessful()) {
                LeadScoringDTO scoring = response.getBody();
                if(scoring!=null){
                    leadEntity.setScore(scoring.getLeadScore());
                    leadEntity.setTemperature(scoring.getLeadTemperature());
                }
            }
        } catch (RestClientException ex) {
            log.error("Failed to fetch lead score: {}", ex.getMessage());
            // Optional: set default score/temperature or handle fallback
        }
        LeadDetailsEntity save = leadDetailsRepository.save(leadEntity);
        return convertToLeadRegistration(save, venue);
    }


    public List<LeadWithVenueDetails> mapToLeadWithVenueDetailsList(Page<LeadRegistration> leads) {
        List<LeadWithVenueDetails> result = new ArrayList<>();

        for (LeadRegistration lead : leads) {
            LeadWithVenueDetails leadWithVenueDetails = new LeadWithVenueDetails();

            // Basic Lead Info
            leadWithVenueDetails.setLeadId(lead.getLeadId());
            leadWithVenueDetails.setFullName(lead.getFullName());
            leadWithVenueDetails.setAge(lead.getAge());
            leadWithVenueDetails.setOccupation(lead.getOccupation());
            leadWithVenueDetails.setMobileNumber(lead.getMobileNumber());
            leadWithVenueDetails.setEmail(lead.getEmail());
            leadWithVenueDetails.setPinCode(lead.getPinCode());
            leadWithVenueDetails.setActive(lead.getActive());
            leadWithVenueDetails.setLineOfBusiness(lead.getLineOfBusiness());
            leadWithVenueDetails.setVerified(lead.getMobileVerified());
            leadWithVenueDetails.setEitherMobileOrEmailPresent(lead.isEitherMobileOrEmailPresent());
            leadWithVenueDetails.setCreatedBy(lead.getCreatedBy());
            leadWithVenueDetails.setCreationDate(lead.getCreationDate() != null ? lead.getCreationDate().toString() : null);
            leadWithVenueDetails.setLastModifiedBy(lead.getLastModifiedBy());
            leadWithVenueDetails.setLastModifiedDate(
                    lead.getLastModifiedDate() != null
                            ? lead.getLastModifiedDate().toString()
                            : LocalDateTime.now().toString()
            );
            leadWithVenueDetails.setIncomeRange(lead.getIncomeRange());
            leadWithVenueDetails.setLifeStage(lead.getLifeStage());
            leadWithVenueDetails.setGender(lead.getGender());
            leadWithVenueDetails.setRemarks(lead.getRemarks());
            leadWithVenueDetails.setMaritalStatus(lead.getMaritalStatus());
            leadWithVenueDetails.setLifeStageMaritalStatus(lead.getMaritalStatus());
            leadWithVenueDetails.setDeleted(lead.getDeleted());
            leadWithVenueDetails.setExistingProducts(lead.getExistingProducts());

            // Address Logic
            Optional<AddressDetailsEntity> addressOpt = addressDetailsRepository.findByLeadId(lead.getLeadId());
            if (addressOpt.isPresent()) {
                AddressDetailsEntity address = addressOpt.get();

                List<String> parts = new ArrayList<>();
                if (address.getPermanentAddressLine1() != null && !address.getPermanentAddressLine1().trim().isEmpty())
                    parts.add(address.getPermanentAddressLine1().trim());
                if (address.getPermanentAddressLine2() != null && !address.getPermanentAddressLine2().trim().isEmpty())
                    parts.add(address.getPermanentAddressLine2().trim());
                if (address.getPermanentCity() != null && !address.getPermanentCity().trim().isEmpty())
                    parts.add(address.getPermanentCity().trim());
                if (address.getPermanentState() != null && !address.getPermanentState().trim().isEmpty())
                    parts.add(address.getPermanentState().trim());
                if (address.getPermanentCountry() != null && !address.getPermanentCountry().trim().isEmpty())
                    parts.add(address.getPermanentCountry().trim());

                String fullAddress = String.join(", ", parts);
                leadWithVenueDetails.setAddress(fullAddress);
                leadWithVenueDetails.setPinCode(address.getPermanentPincode());
            } else {
                // Fallback to string-based address from legacy system
                leadWithVenueDetails.setAddress(lead.getAddress());
            }

            // Venue Details (if present)
            if (lead.getVenue() != null && lead.getVenue().getVenueId() != null) {
                Optional<Venue> venueOpt = venueRepository.findByVenueId(lead.getVenue().getVenueId());
                if (venueOpt.isPresent()) {
                    Venue leadVenue = venueOpt.get();
                    LeadWithVenueDetails.VenueDetails venueDetails = new LeadWithVenueDetails.VenueDetails();
                    venueDetails.setVenueId(leadVenue.getVenueId());
                    venueDetails.setVenueName(leadVenue.getVenueName());
                    venueDetails.setLatitude(leadVenue.getLatitude());
                    venueDetails.setLongitude(leadVenue.getLongitude());
                    venueDetails.setActive(leadVenue.getIsActive());
                    venueDetails.setAddress(leadVenue.getAddress());

                    leadWithVenueDetails.setVenueDetails(venueDetails);
                }
            }

            result.add(leadWithVenueDetails);
        }

        return result;
    }


    private LeadRegistration convertToLeadRegistration(LeadDetailsEntity entity, Venue venue) {
        LeadRegistration registration = new LeadRegistration();

        registration.setLeadId(entity.getId());
        registration.setTitle(entity.getTitle());
        registration.setFirstName(entity.getFirstName());
        registration.setMiddleName(entity.getMiddleName());
        registration.setLastName(entity.getLastName());
        registration.setFullName(entity.getFullName());
        registration.setAge(entity.getAge());
        registration.setDob(entity.getDob());
        registration.setMobileNumber(entity.getMobileNumber());
        registration.setPhoneNumber(entity.getPhoneNumber());
        registration.setCustomerId(entity.getCustomerId());
        registration.setEmail(entity.getEmail());
        registration.setOccupation(entity.getOccupation());
        registration.setGender(entity.getGender());
        registration.setNationality(entity.getNationality());
        registration.setTaxStatus(entity.getTaxStatus());
        registration.setEducation(entity.getEducation());
        registration.setPan(entity.getPan());
        registration.setAadhaar(entity.getAadhaar());
        registration.setLifeStageMaritalStatus(entity.getLifeStageMaritalStatus());
        registration.setStage(entity.getStage());
        registration.setExistingProducts(entity.getExistingProducts());
        registration.setScore(entity.getScore());
        registration.setTemperature(entity.getTemperature());
        registration.setStatus(entity.getStatus());
        registration.setRemarks(entity.getRemarks());
        registration.setSource(entity.getSource());
        registration.setLifeStage(entity.getLifeStage());
        registration.setLineOfBusiness(entity.getLineOfBusiness());
        registration.setMaritalStatus(entity.getLifeStageMaritalStatus());
        registration.setRoleCode(entity.getRoleCode());
        registration.setBranchCode(entity.getBranchCode());
        registration.setChannelCode(entity.getChannelCode());
        registration.setActive(entity.getIsActive());
        registration.setDeleted(entity.getIsDeleted());
        registration.setMobileVerified(entity.getIsMobileVerified());
        registration.setCreationDate(entity.getCreationDate());
        registration.setLastModifiedDate(entity.getLastModifiedDate());
        registration.setCreatedBy(entity.getCreatedBy());
        registration.setLastModifiedBy(entity.getLastModifiedBy());
        registration.setIncomeRange(entity.getIncomeRange());
        AddressDetailsEntity address = entity.getAddressDetailsEntity();
        if (address != null) {
            String fullAddress = String.join(", ",
                    address.getPermanentAddressLine1(),
                    address.getPermanentAddressLine2(),
                    address.getPermanentCity(),
                    address.getPermanentState(),
                    address.getPermanentCountry()
            );
            registration.setAddress(fullAddress);
            registration.setPinCode(address.getPermanentPincode());
        }
        registration.setVenue(venue);
        return registration;
    }


    private AddressDetailsEntity addressMapping(LeadRegistration leadRegistration) {
        AddressDetailsEntity address = new AddressDetailsEntity();
        if (leadRegistration.getAddress() != null && !leadRegistration.getAddress().isEmpty()) {
            String[] parts = leadRegistration.getAddress().split(",");
            address.setPermanentAddressLine1(parts.length > 0 ? parts[0].trim() : "");
            address.setPermanentAddressLine2(parts.length > 1 ? parts[1].trim() : "");
            address.setPermanentCity(parts.length > 2 ? parts[2].trim() : "");
            address.setPermanentState(parts.length > 3 ? parts[3].trim() : "");
            address.setPermanentCountry(parts.length > 4 ? parts[4].trim() : "");
            address.setPermanentPincode(leadRegistration.getPinCode());
            // Optionally set communication same as permanent
            address.setCommunicationAddressLine1(address.getPermanentAddressLine1());
            address.setCommunicationAddressLine2(address.getPermanentAddressLine2());
            address.setCommunicationCity(address.getPermanentCity());
            address.setCommunicationState(address.getPermanentState());
            address.setCommunicationCountry(address.getPermanentCountry());
            address.setCommunicationPincode(address.getPermanentPincode());
        }
        return address;
    }

    private LeadDetailsEntity convertToEntity(LeadRegistration leadRegistration) {
        LeadDetailsEntity leadEntity;

        if (leadRegistration.getLeadId() != null) {
            leadEntity = leadDetailsRepository.findById(leadRegistration.getLeadId())
                    .orElseThrow(() -> new LeadNotFoundException(LEAD_NOT_FOUND + leadRegistration.getLeadId()));
        } else {
            leadEntity = new LeadDetailsEntity();
            String userId = request.getAttribute(USER_ID).toString();
            leadEntity.setCreatedBy(userId);
            leadEntity.setLastModifiedBy(userId);
            leadEntity.setIsActive(true);
            leadEntity.setIsDeleted(false);
            leadEntity.setIsMobileVerified(false);
        }
        leadEntity.setFullName(leadRegistration.getFullName());
        leadEntity.setFirstName(leadRegistration.getFirstName());
        leadEntity.setMiddleName(leadRegistration.getMiddleName());
        leadEntity.setLastName(leadRegistration.getLastName());
        leadEntity.setTitle(leadRegistration.getTitle());
        leadEntity.setAge(leadRegistration.getAge());
        leadEntity.setDob(leadRegistration.getDob());
        leadEntity.setMobileNumber(leadRegistration.getMobileNumber());
        leadEntity.setPhoneNumber(leadRegistration.getPhoneNumber());
        leadEntity.setCustomerId(leadRegistration.getCustomerId());
        leadEntity.setEmail(leadRegistration.getEmail());
        leadEntity.setOccupation(leadRegistration.getOccupation());
        leadEntity.setGender(leadRegistration.getGender());
        leadEntity.setNationality(leadRegistration.getNationality());
        leadEntity.setTaxStatus(leadRegistration.getTaxStatus());
        leadEntity.setEducation(leadRegistration.getEducation());
        leadEntity.setPan(leadRegistration.getPan());
        leadEntity.setAadhaar(leadRegistration.getAadhaar());
        leadEntity.setStage(leadRegistration.getStage());
        leadEntity.setScore(leadRegistration.getScore());
        leadEntity.setTemperature(leadRegistration.getTemperature());
        leadEntity.setStatus(leadRegistration.getStatus());
        leadEntity.setRemarks(leadRegistration.getRemarks());
        leadEntity.setSource(leadRegistration.getSource());
        leadEntity.setLifeStage(leadRegistration.getLifeStage());
        leadEntity.setCreatedAt(LocalDateTime.now());
        leadEntity.setCreationDate(new Date());
        leadEntity.setLastModifiedAt(new Date());
        leadEntity.setLineOfBusiness(leadRegistration.getLineOfBusiness());
        leadEntity.setLifeStageMaritalStatus(leadRegistration.getMaritalStatus());
        leadEntity.setRoleCode(leadRegistration.getRoleCode());
        leadEntity.setBranchCode(leadRegistration.getBranchCode());
        leadEntity.setChannelCode(leadRegistration.getChannelCode());
        return leadEntity;
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
        LeadRegistration existingLead = fetchLeadRegistrationById(leadId);
        String userId = request.getAttribute(USER_ID).toString();


        LeadDetailsEntity existingLeadEntity = convertToEntity(existingLead);
        persistCustomerDetails(userId, existingLead.getCustomerId(), updatedLead, authHeader);
        // Update the fields
        updateNameFields(existingLeadEntity, updatedLead);
        updateLeadFields(existingLeadEntity, updatedLead,userId);
        updateVenueIfRequired(existingLeadEntity, updatedLead);
        updateOrCreateAddress(existingLeadEntity, updatedLead);

        LeadDetailsEntity savedLead = leadDetailsRepository.save(existingLeadEntity);
        logger.info("Updated lead with ID: {}", savedLead.getId());
        return convertToLeadRegistration(savedLead, savedLead.getVenue());
    }

    private void updateOrCreateAddress(LeadDetailsEntity existingLeadEntity, LeadRegistration updatedLead) {
        AddressDetailsEntity address = existingLeadEntity.getAddressDetailsEntity();

        if (address == null) {
            address = new AddressDetailsEntity();
            address.setLeadDetailsEntity(existingLeadEntity); // Set bidirectional link
            existingLeadEntity.setAddressDetailsEntity(address);
        }
        if (updatedLead.getAddress() != null && !updatedLead.getAddress().isEmpty()) {
            String[] parts = updatedLead.getAddress().split(",");
            address.setPermanentAddressLine1(parts.length > 0 ? parts[0].trim() : "");
            address.setPermanentAddressLine2(parts.length > 1 ? parts[1].trim() : "");
            address.setPermanentCity(parts.length > 2 ? parts[2].trim() : "");
            address.setPermanentState(parts.length > 3 ? parts[3].trim() : "");
            address.setPermanentCountry(parts.length > 4 ? parts[4].trim() : "");
            address.setPermanentPincode(updatedLead.getPinCode());

            // Copy to communication
            address.setCommunicationAddressLine1(address.getPermanentAddressLine1());
            address.setCommunicationAddressLine2(address.getPermanentAddressLine2());
            address.setCommunicationCity(address.getPermanentCity());
            address.setCommunicationState(address.getPermanentState());
            address.setCommunicationCountry(address.getPermanentCountry());
            address.setCommunicationPincode(address.getPermanentPincode());
        }

    }

    private void updateVenueIfRequired(LeadDetailsEntity leadEntity, LeadRegistration updatedLead) {
        if (updatedLead.getVenue() != null && updatedLead.getVenue().getVenueId() != null) {
            Venue venue = venueRepository.findByVenueId(updatedLead.getVenue().getVenueId())
                    .orElseThrow(() -> new EntityNotFoundException("Venue not found with id: " + updatedLead.getVenue().getVenueId()));
            leadEntity.setVenue(venue);
        }
    }

    private void updateLeadFields(LeadDetailsEntity entity, LeadRegistration dto,String userId) {
        entity.setEmail(dto.getEmail());
        entity.setMobileNumber(dto.getMobileNumber());
        entity.setStatus(dto.getStatus());
        entity.setIsActive(dto.getActive());
        entity.setAge(dto.getAge());
        entity.setLastModifiedBy(userId);
        entity.setLastModifiedDate(new Date());
        entity.setOccupation(dto.getOccupation());
        entity.setDob(dto.getDob());
        entity.setPinCode(dto.getPinCode());
        entity.setGender(dto.getGender());
        entity.setLifeStage(dto.getLifeStage());
        entity.setLineOfBusiness(dto.getLineOfBusiness());
        entity.setLifeStageMaritalStatus(dto.getMaritalStatus());
        entity.setRemarks(dto.getRemarks());
        entity.setIncomeRange(dto.getIncomeRange());
        entity.setExistingProducts(dto.getExistingProducts());
    }


    private void updateNameFields(LeadDetailsEntity entity, LeadRegistration dto) {
        if (dto.getFullName() != null && !dto.getFullName().isEmpty()) {
            String[] nameParts = dto.getFullName().split(" ");
            entity.setFirstName(nameParts[0]);
            entity.setMiddleName(nameParts.length > 2 ? nameParts[1] : "");
            entity.setLastName(nameParts.length > 1 ? nameParts[nameParts.length - 1] : "");
            entity.setFullName(dto.getFullName());
        }
    }

    private LeadRegistration fetchLeadRegistrationById(Long leadId) {
        return leadRegRepository.findById(leadId)
                .orElseThrow(() -> new LeadNotFoundException(LEAD_NOT_FOUND + leadId));
    }

    public String persistCustomerDetails(String userId, LeadRegistration leadRegistration,String authHeader) {
        logger.info("VenueManagementApp - Inside persistCustomerDetails Method");
        UserMasterRequest userMasterDetails = userMgmtResService.getUserMasterDetails(userId);
        if(userMasterDetails == null){
            return null;
        }
        CustomerRequest customerRequest = new CustomerRequest();
        if (leadRegistration.getFullName() != null && (!leadRegistration.getFullName().isEmpty())) {
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

    @Override
    public List<LeadWithVenueDetails> searchLeadsWithDetails(String query, String userId) {
        List<LeadRegistration> leads = simpleSearchLeads(query, userId);
        List<LeadWithVenueDetails> result = new ArrayList<>();
        for (LeadRegistration lead : leads) {
            LeadWithVenueDetails leadWithVenueDetails = new LeadWithVenueDetails();
            leadWithVenueDetails.setLeadId(lead.getLeadId());
            leadWithVenueDetails.setFullName(lead.getFullName());
            leadWithVenueDetails.setAge(lead.getAge());
            leadWithVenueDetails.setOccupation(lead.getOccupation());
            leadWithVenueDetails.setMobileNumber(lead.getMobileNumber());
            leadWithVenueDetails.setAddress(lead.getAddress()); // Using string field as no address parsing logic is required here
            leadWithVenueDetails.setEmail(lead.getEmail());
            leadWithVenueDetails.setPinCode(lead.getPinCode());
            leadWithVenueDetails.setActive(lead.getActive());
            leadWithVenueDetails.setLineOfBusiness(lead.getLineOfBusiness());
            leadWithVenueDetails.setVerified(lead.getMobileVerified());
            leadWithVenueDetails.setEitherMobileOrEmailPresent(lead.isEitherMobileOrEmailPresent());
            leadWithVenueDetails.setCreatedBy(lead.getCreatedBy());
            leadWithVenueDetails.setCreationDate(
                    lead.getCreationDate() != null ? lead.getCreationDate().toString() : null
            );
            leadWithVenueDetails.setLastModifiedBy(lead.getLastModifiedBy());
            leadWithVenueDetails.setLastModifiedDate(
                    lead.getLastModifiedDate() != null ? lead.getLastModifiedDate().toString() : LocalDateTime.now().toString()
            );

            leadWithVenueDetails.setIncomeRange(lead.getIncomeRange());
            leadWithVenueDetails.setLifeStage(lead.getLifeStage());
            leadWithVenueDetails.setGender(lead.getGender());
            leadWithVenueDetails.setRemarks(lead.getRemarks());
            leadWithVenueDetails.setMaritalStatus(lead.getMaritalStatus());
            leadWithVenueDetails.setDeleted(lead.getDeleted());
            leadWithVenueDetails.setExistingProducts(lead.getExistingProducts());

            Optional<AddressDetailsEntity> addressOpt = addressDetailsRepository.findByLeadId(lead.getLeadId());
            if (addressOpt.isPresent()) {
                AddressDetailsEntity address = addressOpt.get();
                List<String> parts = new ArrayList<>();
                if (address.getPermanentAddressLine1() != null && !address.getPermanentAddressLine1().trim().isEmpty())
                    parts.add(address.getPermanentAddressLine1().trim());
                if (address.getPermanentAddressLine2() != null && !address.getPermanentAddressLine2().trim().isEmpty())
                    parts.add(address.getPermanentAddressLine2().trim());
                if (address.getPermanentCity() != null && !address.getPermanentCity().trim().isEmpty())
                    parts.add(address.getPermanentCity().trim());
                if (address.getPermanentState() != null && !address.getPermanentState().trim().isEmpty())
                    parts.add(address.getPermanentState().trim());
                if (address.getPermanentCountry() != null && !address.getPermanentCountry().trim().isEmpty())
                    parts.add(address.getPermanentCountry().trim());

                String fullAddress = String.join(", ", parts);
                leadWithVenueDetails.setAddress(fullAddress);
                leadWithVenueDetails.setPinCode(address.getPermanentPincode());
            } else {
                // fallback to legacy string-based address
                leadWithVenueDetails.setAddress(lead.getAddress());
                leadWithVenueDetails.setPinCode(lead.getPinCode());
            }
            if (lead.getVenue() != null && lead.getVenue().getVenueId() != null) {
                Optional<Venue> venueOpt = venueRepository.findByVenueId(lead.getVenue().getVenueId());
                if (venueOpt.isPresent()) {
                    Venue leadVenue = venueOpt.get();
                    LeadWithVenueDetails.VenueDetails venueDetails = new LeadWithVenueDetails.VenueDetails();
                    venueDetails.setVenueId(leadVenue.getVenueId());
                    venueDetails.setVenueName(leadVenue.getVenueName());
                    venueDetails.setLatitude(leadVenue.getLatitude());
                    venueDetails.setLongitude(leadVenue.getLongitude());
                    venueDetails.setActive(leadVenue.getIsActive());
                    venueDetails.setAddress(leadVenue.getAddress());
                    leadWithVenueDetails.setVenueDetails(venueDetails);
                }
            }
            result.add(leadWithVenueDetails);
        }
        return result;
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
                .orElseThrow(() -> new LeadNotFoundException(LEAD_NOT_FOUND + leadId));
        CustomerServiceClient customerServiceClient = new CustomerServiceClient(new RestTemplate());
        String customerId = lead.getCustomerId();
        customerServiceClient.deleteCustomer(customerId,authHeader);
        lead.setDeleted(true);
        lead.setActive(false);
        leadRegRepository.save(lead);
        logger.info("Marked lead with ID: {} as deleted", leadId);
    }

}
