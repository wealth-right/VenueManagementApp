package com.venue.mgmt.controller;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.repositories.VenueRepository;
import com.venue.mgmt.response.ApiResponse;
import com.venue.mgmt.response.PaginationDetails;
import com.venue.mgmt.services.LeadRegistrationService;
import com.venue.mgmt.services.OTPService;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.text.SimpleDateFormat;
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

    @Autowired
    private LeadRegistrationService leadRegistrationService;

    @Autowired
    private OTPService otpService;

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private HttpServletRequest request;

    @PostMapping
    @Operation(summary = "Create a new lead", description = "Creates a new lead with the provided details and sends OTP for verification")
    public ResponseEntity<LeadRegistration> createLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @Valid @RequestBody LeadRegistration leadRegistration) throws Exception {

        logger.info("VenueManagementApp - Inside create Lead Method");
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if(tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                logger.warn("Token is expired");
                return ResponseEntity.status(401).build();
            }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            request.setAttribute("userId", userId);

            leadRegistration.setActive(true);
            leadRegistration.setCreatedBy(userId);
            LeadRegistration savedLead = leadRegistrationService.saveLead(leadRegistration);
            return ResponseEntity.ok(savedLead);
        }
        return ResponseEntity.status(401).build();
    }


    @GetMapping
    @Operation(summary = "Get all leads", description = "Retrieves all leads with pagination support")
    public ResponseEntity<ApiResponse<Page<LeadRegistration>>> getAllLeads(
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

            Page<LeadRegistration> leads = leadRegistrationService.getAllLeadsSortedByCreationDateAndCreatedByAndVenueIdAndDateRange
                    (sort, page, size, userId,venueId,start,end);
            ResponseEntity<Page<LeadRegistration>> responseEntity = ResponseEntity.ok(leads);
            ApiResponse<Page<LeadRegistration>> response = new ApiResponse<>();
            response.setStatusCode(responseEntity.getStatusCode().value());
            response.setStatusMsg("Success");

            response.setErrorMsg(null);
            response.setResponse(leads.getContent());
            if (venueId != null) {
                response.setVenueDetails(venueRepository.findById(venueId).orElse(null));
            }
            PaginationDetails paginationDetails = new PaginationDetails();
            paginationDetails.setCurrentPage(leads.getNumber());
            paginationDetails.setTotalRecords(leads.getTotalElements());
            paginationDetails.setTotalPages(leads.getTotalPages());
            response.setPaginationDetails(paginationDetails);
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
            response.setStatusMsg("Success");
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
        if (tokenValid){
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
