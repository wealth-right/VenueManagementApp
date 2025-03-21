package com.venue.mgmt.controller;

import com.venue.mgmt.constant.GeneralMsgConstants;
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
import com.venue.mgmt.util.JWTValidator;
import com.venue.mgmt.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import static com.venue.mgmt.constant.GeneralMsgConstants.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    private HttpServletRequest request;


    public LeadRegistrationController(LeadRegistrationService leadRegistrationService, VenueRepository venueRepository,HttpServletRequest request) {
        this.leadRegistrationService = leadRegistrationService;
        this.venueRepository = venueRepository;
        this.request = request;
    }

    @PostMapping
    @Operation(summary = "Create a new lead", description = "Creates a new lead with the provided details and sends OTP for verification")
    public ResponseEntity<LeadResponse<LeadRegistration>> createLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @Valid @RequestBody LeadRegistration leadRegistration) throws Exception {

        logger.info("VenueManagementApp - Inside create Lead Method");
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                logger.warn(TOKEN_EXPIRED);
                return ResponseEntity.status(401).build();
            }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            request.setAttribute(USER_ID, userId);

            // Fetch user details from the API
            CustomerServiceClient custServiceClient = new CustomerServiceClient(new RestTemplate());
            UserDetailsResponse.UserDetails userDetails = custServiceClient.getUserDetails(userId);
            if (userDetails == null) {
                return ResponseEntity.status(404).body(null);
            }
            // Create CustomerRequest object
            CustomerRequest customerRequest = new CustomerRequest();
            customerRequest.setTitle("Mr.");
            customerRequest.setFirstname(leadRegistration.getFullName().split(" ")[0]);
            customerRequest.setMiddlename(leadRegistration.getFullName().split(" ").length > 2 ? leadRegistration.getFullName().split(" ")[1] : "");
            customerRequest.setLastname(leadRegistration.getFullName().split(" ").length > 1 ? leadRegistration.getFullName().split(" ")[leadRegistration.getFullName().split(" ").length - 1] : "");
            customerRequest.setFullname(leadRegistration.getFullName());
            customerRequest.setEmailid(leadRegistration.getEmail());
            customerRequest.setCountrycode("+91");
            customerRequest.setMobileno(leadRegistration.getMobileNumber());
            customerRequest.setAddedby(userId);
            customerRequest.setAssignedto(userId);
//            customerRequest.setDob(leadRegistration.getDob().toString());
            customerRequest.setGender(leadRegistration.getGender().substring(0, 1).toLowerCase());
            customerRequest.setOccupation("01");
            customerRequest.setTaxStatus("01");
            customerRequest.setCountryOfResidence("India");
            customerRequest.setSource("QuickTapApp");
            customerRequest.setCustomertype("Prospect");
            customerRequest.setChannelcode(userDetails.getChannelcode());

            // Save customer data
            CustomerServiceClient customerServiceClient = new CustomerServiceClient(new RestTemplate());
            customerServiceClient.saveCustomerData(customerRequest);
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
        return ResponseEntity.status(401).build();
    }


    @GetMapping
    @Operation(summary = "Get all leads", description = "Retrieves all leads with pagination support")
    public ResponseEntity<ApiResponse<Page<LeadWithVenueDetails>>> getAllLeads(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long venueId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) throws Exception {


        logger.info("VenueManagementApp - Inside get All Leads Method with sort: {}, page: {}, size: {}", sort, page, size);

        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                return ResponseEntity.status(401).build();
            }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            request.setAttribute("userId", userId);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            Date start = startDate != null ? formatter.parse(startDate) : null;
            Date end = endDate != null ? formatter.parse(endDate) : null;

            // Adjust end date to include the entire day if start and end dates are the same
            if (start != null && end != null && start.equals(end)) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(end);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                end = calendar.getTime();
            }
            Page<LeadRegistration> leads = leadRegistrationService.getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRange
                    (sort, page, size, userId, venueId, start, end);

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
                        leadWithVenueDetails.setActive(lead.getActive());
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
                        // Fetch venue details for each lead
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
            paginationDetails.setCurrentPage(leads.getNumber());
            paginationDetails.setTotalRecords(leads.getTotalElements());
            paginationDetails.setTotalPages(leads.getTotalPages());
            response.setPagination(paginationDetails);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<LeadRegistration>>> searchLeads(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(required = false) String query) throws Exception {
        logger.info("VenueManagementApp - Inside search Leads Method with query: {}", query);
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                return ResponseEntity.status(401).build();
            }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            request.setAttribute("userId", userId);
            List<LeadRegistration> leads = leadRegistrationService.simpleSearchLeads(query, userId);
            ResponseEntity<List<LeadRegistration>> ok = ResponseEntity.ok(leads);
            ApiResponse<List<LeadRegistration>> response = new ApiResponse<>();
            response.setStatusCode(ok.getStatusCodeValue());
            response.setStatusMsg(SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(leads);
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }

    @PutMapping("/{leadId}")
    @Operation(summary = "Update a lead", description = "Updates an existing lead with the provided details")
    public ResponseEntity<LeadRegistration> updateLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Long leadId,
            @Valid @RequestBody LeadRegistration leadRegistration) throws Exception {

        logger.info("VenueManagementApp - Inside update Lead Method for leadId: {}", leadId);
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                return ResponseEntity.status(401).build();
            }

            try {
                LeadRegistration updatedLead = leadRegistrationService.updateLead(leadId, leadRegistration);
                return ResponseEntity.ok(updatedLead);
            } catch (RuntimeException e) {
                logger.error("Error updating lead: {}", e.getMessage());
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.status(401).build();
    }


    @DeleteMapping("/{leadId}")
    @Operation(summary = "Delete a lead", description = "Deletes an existing lead by its ID")
    public ResponseEntity<Void> deleteLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Long leadId) throws Exception {

        logger.info("VenueManagementApp - Inside delete Lead Method for leadId: {}", leadId);
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                return ResponseEntity.status(401).build();
            }
            try {
                leadRegistrationService.deleteLead(leadId);
                return ResponseEntity.ok().build();
            } catch (RuntimeException e) {
                logger.error("Error deleting lead: {}", e.getMessage());
                return ResponseEntity.notFound().build();
            }
        }
        return ResponseEntity.status(401).build();
    }
}
