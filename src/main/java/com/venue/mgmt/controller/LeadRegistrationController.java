package com.venue.mgmt.controller;

import com.venue.mgmt.dto.LeadSearchCriteria;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.services.LeadRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
@RequestMapping("/venue-app/v1/leads")
@Tag(name = "Lead Registration Controller", description = "API to fetch details of Leads")
@Slf4j
public class LeadRegistrationController {

    private static final Logger logger = LogManager.getLogger(LeadRegistrationController.class);

    @Autowired
    private LeadRegistrationService leadRegistrationService;

    @PostMapping
    public ResponseEntity<LeadRegistration> createLead(@RequestBody @Valid LeadRegistration leadRegistration) {
        logger.info("VenueManagementApp - Inside create Lead Method");
        leadRegistration.setActive(true);
        LeadRegistration savedLead = leadRegistrationService.saveLead(leadRegistration);
        if(savedLead!=null){
            return ResponseEntity.ok(savedLead);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping
    public ResponseEntity<Iterable<LeadRegistration>> getAllLeads(
            @RequestParam(required = false, defaultValue = "desc") String sort) {
        logger.info("VenueManagementApp - Inside get All Leads Method with sort: {}", sort);
        Iterable<LeadRegistration> leads = leadRegistrationService.getAllLeadsSortedByCreationDate(sort);
        if(leads!=null && leads.iterator().hasNext()){
            return ResponseEntity.ok(leads);
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search leads with filters, sorting and pagination")
    public ResponseEntity<Page<LeadRegistration>> searchLeads(
            @Parameter(description = "Filter by full name (case-insensitive, partial match)")
            @RequestParam(required = false) String fullName,
            
            @Parameter(description = "Filter by email (case-insensitive, partial match)")
            @RequestParam(required = false) String email,
            
            @Parameter(description = "Filter by mobile number (partial match)")
            @RequestParam(required = false) String mobile,
            
            @Parameter(description = "Field to sort by (fullName, email, mobileNumber, createdDate)")
            @RequestParam(defaultValue = "fullName") String sortBy,
            
            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String sortDirection,
            
            @Parameter(description = "Page number (0-based)")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Number of records per page")
            @RequestParam(defaultValue = "10") int size) {
        
        logger.info("VenueManagementApp - Inside search Leads Method with fullName: {}, email: {}, mobile: {}", 
            fullName, email, mobile);
        
        LeadSearchCriteria criteria = new LeadSearchCriteria();
        criteria.setFullName(fullName);
        criteria.setEmail(email);
        criteria.setMobile(mobile);
        criteria.setSortBy(sortBy);
        criteria.setSortDirection(sortDirection);
        criteria.setPage(page);
        criteria.setSize(size);

        logger.info("Search criteria: {}", criteria);

        Page<LeadRegistration> leads = leadRegistrationService.searchLeads(criteria);
        
        if (leads != null && leads.hasContent()) {
            logger.info("Found {} leads matching the criteria", leads.getTotalElements());
            return ResponseEntity.ok(leads);
        }
        logger.info("No leads found matching the criteria");
        return ResponseEntity.ok(Page.empty());
    }

}
