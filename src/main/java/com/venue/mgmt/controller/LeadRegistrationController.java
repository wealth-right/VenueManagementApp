package com.venue.mgmt.controller;

import com.venue.mgmt.dto.LeadWithVenueDetails;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.request.CustomerDetailsClient;
import com.venue.mgmt.response.ApiResponse;
import com.venue.mgmt.response.LeadResponse;
import com.venue.mgmt.response.PaginationDetails;
import com.venue.mgmt.services.LeadRegistrationService;
import com.venue.mgmt.util.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.venue.mgmt.constant.GeneralMsgConstants.SUCCESS;
import static com.venue.mgmt.constant.GeneralMsgConstants.USER_ID;

@RestController
@RequestMapping("/venue-app/v1/leads")
@Tag(name = "Lead Registration Controller", description = "API to manage lead registrations")
@SecurityRequirement(name = "bearerAuth")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
@Slf4j
public class LeadRegistrationController {

    private static final Logger logger = LogManager.getLogger(LeadRegistrationController.class);

    private final LeadRegistrationService leadRegistrationService;


    private final VenueRepository venueRepository;

    private final HttpServletRequest request;


    private final CustomerDetailsClient customerDetailsClient;




    public LeadRegistrationController(LeadRegistrationService leadRegistrationService, VenueRepository venueRepository,
                                      HttpServletRequest request, CustomerDetailsClient customerDetailsClient) {
        this.leadRegistrationService = leadRegistrationService;
        this.venueRepository = venueRepository;
        this.request = request;
        this.customerDetailsClient = customerDetailsClient;
    }

    @PostMapping
    @Operation(summary = "Create a new lead", description = "Creates a new lead with the provided details and sends OTP for verification")
    @Transactional
    public ResponseEntity<LeadResponse<LeadRegistration>> createLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @Valid @RequestBody LeadRegistration leadRegistration) {
            logger.info("VenueManagementApp - Inside create Lead Method");
            logger.info("{}", leadRegistration);
            String userId = request.getAttribute(USER_ID).toString();
            String existingCustomerId = customerDetailsClient.getCustomerId(leadRegistration.getMobileNumber());
            String customerId   = null;
            if( existingCustomerId != null && !existingCustomerId.isEmpty()) {
                logger.info("Customer already exists with ID: {}", existingCustomerId);
                leadRegistration.setCustomerId(existingCustomerId);
            } else {
                logger.info("No existing customer found for mobile number: {}", leadRegistration.getMobileNumber());
                String customerDetails = leadRegistrationService.persistCustomerDetails(userId, leadRegistration,authHeader);
                 customerId = CommonUtils.extractCustomerId(customerDetails);
                 leadRegistration.setCustomerId(customerId);
            }
            LeadRegistration savedLead = leadRegistrationService.saveLead(leadRegistration);
            LeadResponse<LeadRegistration> response = new LeadResponse<>();
            response.setStatusCode(200);
            response.setStatusMsg(SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(savedLead);
            return ResponseEntity.ok(response);
    }




    @GetMapping
    @Operation(summary = "Get all leads", description = "Retrieves all leads with pagination support")
    public ResponseEntity<ApiResponse<Page<LeadWithVenueDetails>>> getAllLeads(
            @PageableDefault(sort = "created_at", direction = Sort.Direction.DESC, page = 1, size = 20) Pageable pageable,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) throws ParseException {

        logger.info("VenueManagementApp - Inside get All Leads Method with pageable: {}", pageable);

        String userId = (String) request.getAttribute("userId");

        // Adjust page number to start from 1
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date start = startDate != null ? formatter.parse(startDate) : null;
        Date end = endDate != null ? formatter.parse(endDate) : null;
        if (start != null && end != null && start.equals(end)) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(end);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            end = calendar.getTime();
        }
        Page<LeadRegistration> leads = leadRegistrationService.getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRangeAndIsDeletedFalse
                (pageable.getSort().toString(), pageable.getPageNumber(), pageable.getPageSize(), userId, venueId, start, end);
        List<LeadWithVenueDetails> leadWithVenueDetailsList = leadRegistrationService.mapToLeadWithVenueDetailsList(leads);
        ApiResponse<Page<LeadWithVenueDetails>> response = new ApiResponse<>();
        response.setStatusCode(200);
        response.setStatusMsg(SUCCESS);
        response.setErrorMsg(null);
        response.setResponse(leadWithVenueDetailsList);

        PaginationDetails paginationDetails = new PaginationDetails();
        paginationDetails.setCurrentPage(leads.getNumber() + 1);
        paginationDetails.setTotalRecords(leads.getTotalElements());
        paginationDetails.setTotalPages(leads.getTotalPages());
        response.setPagination(paginationDetails);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LeadWithVenueDetails>>> searchLeads(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(required = false) String query)   {
        logger.info("VenueManagementApp - Inside search Leads Method with query: {}", query);
            String userId = (String) request.getAttribute(USER_ID);
            List<LeadRegistration> leads = leadRegistrationService.simpleSearchLeads(query, userId);
            List<LeadWithVenueDetails> leadWithVenueDetailsList = leads.stream()
                    .map(lead -> {
                        LeadWithVenueDetails leadWithVenueDetails = new LeadWithVenueDetails();
                        leadWithVenueDetails.setLeadId(lead.getLeadId());
                        leadWithVenueDetails.setFullName(lead.getFullName());
                        leadWithVenueDetails.setAge(lead.getAge());
                        leadWithVenueDetails.setOccupation(lead.getOccupation());
                        leadWithVenueDetails.setMobileNumber(lead.getMobileNumber());
                        leadWithVenueDetails.setAddress(lead.getAddress());
                        leadWithVenueDetails.setEmail(lead.getEmail());
                        leadWithVenueDetails.setPinCode(lead.getPinCode());
                        leadWithVenueDetails.setActive(lead.getActive());
                        leadWithVenueDetails.setLineOfBusiness(lead.getLineOfBusiness());
                        leadWithVenueDetails.setVerified(lead.getMobileVerified());
                        leadWithVenueDetails.setEitherMobileOrEmailPresent(lead.isEitherMobileOrEmailPresent());
                        leadWithVenueDetails.setCreatedBy(lead.getCreatedBy());
                        leadWithVenueDetails.setCreationDate(lead.getCreationDate().toString());
                        leadWithVenueDetails.setLastModifiedBy(lead.getLastModifiedBy());
                        leadWithVenueDetails.setLastModifiedDate(lead.getLastModifiedDate().toString());
                        leadWithVenueDetails.setIncomeRange(lead.getIncomeRange());
                        leadWithVenueDetails.setLifeStage(lead.getLifeStage());
                        leadWithVenueDetails.setGender(lead.getGender());
                        leadWithVenueDetails.setRemarks(lead.getRemarks());
                        leadWithVenueDetails.setMaritalStatus(lead.getMaritalStatus());
                        leadWithVenueDetails.setDeleted(lead.getDeleted());
                        leadWithVenueDetails.setExistingProducts(lead.getExistingProducts());
                        Venue leadVenue = venueRepository.findByVenueId(lead.getVenue().getVenueId()).orElse(null);
                        if (leadVenue != null) {
                            LeadWithVenueDetails.VenueDetails venueDetails = new LeadWithVenueDetails.VenueDetails();
                            venueDetails.setVenueId(leadVenue.getVenueId());
                            venueDetails.setVenueName(leadVenue.getVenueName());
                            venueDetails.setLatitude(leadVenue.getLatitude());
                            venueDetails.setLongitude(leadVenue.getLongitude());
                            venueDetails.setActive(leadVenue.getIsActive());
                            venueDetails.setAddress(leadVenue.getAddress());
                            leadWithVenueDetails.setVenueDetails(venueDetails);
                        }
                        return leadWithVenueDetails;
                    })
                    .toList();
            ApiResponse<List<LeadWithVenueDetails>> response = new ApiResponse<>();
              response.setStatusCode(200);
            response.setStatusMsg(SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(leadWithVenueDetailsList);
            return ResponseEntity.ok(response);
    }

    @PutMapping("/{leadId}")
    @Operation(summary = "Update a lead", description = "Updates an existing lead with the provided details")
    public ResponseEntity<LeadResponse<LeadRegistration>> updateLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Long leadId,
            @Valid @RequestBody LeadRegistration leadRegistration)  {
        logger.info("{}",leadRegistration);
        logger.info("VenueManagementApp - Inside update Lead Method for leadId: {}", leadId);
            LeadRegistration updatedLead = leadRegistrationService.updateLead(leadId, leadRegistration,authHeader);
            LeadResponse<LeadRegistration> response = new LeadResponse<>();
            response.setStatusCode(200);
            response.setStatusMsg(SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(updatedLead);
            return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{leadId}")
    @Operation(summary = "Delete a lead", description = "Deletes an existing lead by its ID")
    public ResponseEntity<Void> deleteLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Long leadId) {
        logger.info("VenueManagementApp - Inside delete Lead Method for leadId: {}", leadId);
        try {
            leadRegistrationService.deleteLead(leadId,authHeader);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting lead: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
