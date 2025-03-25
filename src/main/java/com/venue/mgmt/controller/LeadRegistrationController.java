package com.venue.mgmt.controller;

import com.venue.mgmt.dto.LeadWithVenueDetails;
import com.venue.mgmt.dto.UserDetailsResponse;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.request.CustomerRequest;
import com.venue.mgmt.request.CustomerServiceClient;
import com.venue.mgmt.response.ApiResponse;
import com.venue.mgmt.response.LeadResponse;
import com.venue.mgmt.response.PaginationDetails;
import com.venue.mgmt.services.LeadRegistrationService;
import com.venue.mgmt.services.UserMgmtResService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

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

    private final UserMgmtResService userMgmtResService;


    public LeadRegistrationController(LeadRegistrationService leadRegistrationService, VenueRepository venueRepository, HttpServletRequest request, UserMgmtResService userMgmtResService) {
        this.leadRegistrationService = leadRegistrationService;
        this.venueRepository = venueRepository;
        this.request = request;
        this.userMgmtResService = userMgmtResService;
    }

    @PostMapping
    @Operation(summary = "Create a new lead", description = "Creates a new lead with the provided details and sends OTP for verification")
    public ResponseEntity<LeadResponse<LeadRegistration>> createLead(
            @Valid @RequestBody LeadRegistration leadRegistration) {

        String userId = request.getAttribute(USER_ID).toString();
        // Create CustomerRequest object
        persistCustomerDetails(userId, leadRegistration);
        leadRegistration.setActive(true);
        leadRegistration.setCreatedBy(userId);
        LeadRegistration savedLead = leadRegistrationService.saveLead(leadRegistration);

        LeadResponse<LeadRegistration> response = new LeadResponse<>();
        response.setStatusCode(200);
        response.setStatusMsg(SUCCESS);
        response.setErrorMsg(null);
        response.setResponse(savedLead);
        return ResponseEntity.ok(response);
    }

    private void persistCustomerDetails(String userId, LeadRegistration leadRegistration) {
        // Fetch user details from the API
        CustomerServiceClient custServiceClient = new CustomerServiceClient(new RestTemplate());
        UserDetailsResponse.UserDetails userDetails = custServiceClient.getUserDetails(userId);
        if (userDetails == null) {
            return;
        }
        // Create CustomerRequest object
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
        customerRequest.setAssignedto(userId);
        if (leadRegistration.getGender() != null && (!leadRegistration.getGender().isEmpty())) {
            customerRequest.setGender(leadRegistration.getGender().substring(0, 1).toLowerCase());
            if (leadRegistration.getGender().equalsIgnoreCase("M")) {
                customerRequest.setTitle("Mr.");
            } else if (leadRegistration.getGender().equalsIgnoreCase("F") &&
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
        String branchCode = userDetails.getBranchCode();
        customerRequest.setBranchCode(userDetails.getBranchCode());
        userMgmtResService.getDataFromOtherSchema(branchCode);
        // Save customer data
        CustomerServiceClient customerServiceClient = new CustomerServiceClient(new RestTemplate());
        ResponseEntity<String> entity = customerServiceClient.saveCustomerData(customerRequest);
        entity.getBody();
    }


    @GetMapping
    @Operation(summary = "Get all leads", description = "Retrieves all leads with pagination support")
    public ResponseEntity<ApiResponse<Page<LeadWithVenueDetails>>> getAllLeads(
            @PageableDefault(sort = "creationDate", direction = Sort.Direction.DESC, page = 1, size = 20) Pageable pageable,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) throws Exception {

        logger.info("VenueManagementApp - Inside get All Leads Method with pageable: {}", pageable);

        String userId = (String) request.getAttribute("userId");

        // Adjust page number to start from 1
        pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date start = startDate != null ? formatter.parse(startDate) : null;
        Date end = endDate != null ? formatter.parse(endDate) : null;
        try {
            if (start != null && end != null && start.equals(end)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(end);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                end = calendar.getTime();
            }
            Page<LeadRegistration> leads = leadRegistrationService.getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRange
                    (pageable.getSort().toString(), pageable.getPageNumber(), pageable.getPageSize(), userId, venueId, start, end);

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
                        leadWithVenueDetails.setVerified(lead.getVerified());
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
                        Venue leadVenue = venueRepository.findById(lead.getVenue().getVenueId()).orElse(null);
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
        } catch (Exception e) {
            logger.error("Error getting all leads: {}", e.getMessage());
            ApiResponse<Page<LeadWithVenueDetails>> response = new ApiResponse<>();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setStatusMsg(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LeadWithVenueDetails>>> searchLeads(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(required = false) String query) throws Exception {
        logger.info("VenueManagementApp - Inside search Leads Method with query: {}", query);
        try {
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
                        leadWithVenueDetails.setVerified(lead.getVerified());
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
                        Venue leadVenue = venueRepository.findById(lead.getVenue().getVenueId()).orElse(null);
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
        } catch (Exception e) {
            logger.error("Error searching leads: {}", e.getMessage());
            ApiResponse<List<LeadWithVenueDetails>> response = new ApiResponse<>();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setStatusMsg(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{leadId}")
    @Operation(summary = "Update a lead", description = "Updates an existing lead with the provided details")
    public ResponseEntity<LeadResponse<LeadRegistration>> updateLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Long leadId,
            @Valid @RequestBody LeadRegistration leadRegistration) throws Exception {

        logger.info("VenueManagementApp - Inside update Lead Method for leadId: {}", leadId);

        try {
            LeadRegistration updatedLead = leadRegistrationService.updateLead(leadId, leadRegistration);
            LeadResponse<LeadRegistration> response = new LeadResponse<>();
            response.setStatusCode(200);
            response.setStatusMsg(SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(updatedLead);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            logger.error("Error updating lead: {}", e.getMessage());
            LeadResponse<LeadRegistration> response = new LeadResponse<>();
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setStatusMsg(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }


    @DeleteMapping("/{leadId}")
    @Operation(summary = "Delete a lead", description = "Deletes an existing lead by its ID")
    public ResponseEntity<Void> deleteLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Long leadId) {

        logger.info("VenueManagementApp - Inside delete Lead Method for leadId: {}", leadId);
        try {
            leadRegistrationService.deleteLead(leadId);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            logger.error("Error deleting lead: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
