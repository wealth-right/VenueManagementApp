package com.venue.mgmt.controller;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.services.LeadRegistrationService;
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
    private HttpServletRequest request;

    @PostMapping
    @Operation(summary = "Create a new lead", description = "Creates a new lead with the provided details")
    public ResponseEntity<LeadRegistration> createLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @Valid @RequestBody LeadRegistration leadRegistration) {

        logger.info("VenueManagementApp - Inside create Lead Method");
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

    @GetMapping
    @Operation(summary = "Get all leads", description = "Retrieves all leads with pagination support")
    public ResponseEntity<Page<LeadRegistration>> getAllLeads(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {

        logger.info("VenueManagementApp - Inside get All Leads Method with sort: {}, page: {}, size: {}", sort, page, size);

        boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
        if (isTokenExpired) {
            return ResponseEntity.status(401).build();
        }

        Page<LeadRegistration> leads = leadRegistrationService.getAllLeadsSortedByCreationDate(sort, page, size);
        return ResponseEntity.ok(leads);
    }

    @GetMapping("/search")
    public ResponseEntity<List<LeadRegistration>> searchLeads(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(required = false) String query) {

        logger.info("VenueManagementApp - Inside search Leads Method with query: {}", query);
        boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
        if (isTokenExpired) {
            return ResponseEntity.status(401).build();
        }

        List<LeadRegistration> leads = leadRegistrationService.simpleSearchLeads(query);
        return ResponseEntity.ok(leads);
    }

    @PutMapping("/{leadId}")
    @Operation(summary = "Update a lead", description = "Updates an existing lead with the provided details")
    public ResponseEntity<LeadRegistration> updateLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Long leadId,
            @Valid @RequestBody LeadRegistration leadRegistration) {

        logger.info("VenueManagementApp - Inside update Lead Method for leadId: {}", leadId);

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


    @DeleteMapping("/{leadId}")
    @Operation(summary = "Delete a lead", description = "Deletes an existing lead by its ID")
    public ResponseEntity<Void> deleteLead(
            @RequestHeader(name = "Authorization") String authHeader,
            @PathVariable Long leadId) {

        logger.info("VenueManagementApp - Inside delete Lead Method for leadId: {}", leadId);

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
}
