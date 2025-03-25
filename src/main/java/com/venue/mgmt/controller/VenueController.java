package com.venue.mgmt.controller;

import com.venue.mgmt.constant.ErrorMsgConstants;
import com.venue.mgmt.constant.GeneralMsgConstants;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.response.ApiResponse;
import com.venue.mgmt.response.PaginationDetails;
import com.venue.mgmt.response.VenueResponse;
import com.venue.mgmt.services.VenueService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venue-app/v1/venues")
public class VenueController {

    private static final Logger logger = LogManager.getLogger(VenueController.class);

    private final VenueService venueService;

    private final HttpServletRequest request;


    public VenueController(VenueService venueService, HttpServletRequest request) {
        this.venueService = venueService;
        this.request = request;
    }

    @PostMapping
    public ResponseEntity<VenueResponse<Venue>> createVenue(
            @RequestHeader(name = "Authorization") String authHeader,
            @Valid @RequestBody Venue venue) {

        String userId = (String) request.getAttribute(GeneralMsgConstants.USER_ID);
        try {
            venue.setCreatedBy(userId);
            Venue savedVenue = venueService.saveVenue(venue);
            VenueResponse<Venue> response = new VenueResponse<>();
            response.setStatusCode(200);
            response.setStatusMsg(GeneralMsgConstants.SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(savedVenue);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            VenueResponse<Venue> response = new VenueResponse<>();
            response.setStatusCode(500);
            response.setStatusMsg("Error while saving the venue");
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Venue>>> getAllVenues(
            @RequestHeader(name = "Authorization") String authHeader,
            @PageableDefault(sort = "creationDate", direction = Sort.Direction.DESC, page = 1, size = 20) Pageable pageable) {
        logger.info("VenueManagementApp - Inside get All Venues Method");
        try {
            String userId = (String) request.getAttribute(GeneralMsgConstants.USER_ID);
            pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());

            Page<Venue> venues = venueService.getAllVenuesSortedByCreationDate(pageable.getSort().toString(), pageable.getPageNumber(), pageable.getPageSize(), userId);
            venues.forEach(venue -> {
                venue.setLeadCount(venue.getLeads().size());
                venue.setLeadCountToday(venue.getLeadCountToday());
            });
            ResponseEntity<Page<Venue>> responseEntity = ResponseEntity.ok(venues);
            ApiResponse<Page<Venue>> response = new ApiResponse<>();
            response.setStatusCode(responseEntity.getStatusCode().value());
            response.setStatusMsg(GeneralMsgConstants.SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(venues.getContent());

            PaginationDetails paginationDetails = new PaginationDetails();
            paginationDetails.setCurrentPage(venues.getNumber() + 1);
            paginationDetails.setTotalRecords(venues.getTotalElements());
            paginationDetails.setTotalPages(venues.getTotalPages());
            response.setPagination(paginationDetails);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Page<Venue>> response = new ApiResponse<>();
            response.setStatusCode(500);
            response.setStatusMsg(ErrorMsgConstants.FAILED);
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Venue>>> searchVenues(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(required = false) String query) {
        String userId = (String) request.getAttribute(GeneralMsgConstants.USER_ID);
        try {
            List<Venue> venues = venueService.searchVenues(query, userId);
            venues.forEach(venue -> {
                venue.setLeadCount(venue.getLeads().size());
                venue.setLeadCountToday(venue.getLeadCountToday());
            });
            ResponseEntity<List<Venue>> responseEntity = ResponseEntity.ok(venues);
            ApiResponse<List<Venue>> response = new ApiResponse<>();
            response.setStatusCode(responseEntity.getStatusCode().value());
            response.setStatusMsg(GeneralMsgConstants.SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(venues);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<Venue>> response = new ApiResponse<>();
            response.setStatusCode(500);
            response.setStatusMsg(ErrorMsgConstants.FAILED);
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<Venue>>> getVenueDetailsByIds(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(name = "venueIds") List<Long> venueIds) {

        try {
            List<Venue> venues = venueService.getVenuesByIds(venueIds);
            venues.forEach(venue -> {
                venue.setLeadCount(venue.getLeads().size());
                venue.setLeadCountToday(venue.getLeadCountToday());
            });
            ApiResponse<List<Venue>> response = new ApiResponse<>();
            response.setStatusCode(200);
            response.setStatusMsg(GeneralMsgConstants.SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(venues);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<List<Venue>> response = new ApiResponse<>();
            response.setStatusCode(500);
            response.setStatusMsg(ErrorMsgConstants.FAILED);
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.internalServerError().body(response);
        }
    }


    @PostMapping("/leads")
    public ResponseEntity<Venue> addLeadToVenue(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestBody LeadRegistration leadRegistration) {
        return ResponseEntity.ok(venueService.addLeadToVenue(leadRegistration));
    }


    @GetMapping("/sorted")
    public ResponseEntity<Page<Venue>> getAllVenuesSorted(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestParam(defaultValue = "creationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Venue> venues = venueService.getAllVenuesSorted(sortBy, sortDirection, latitude, longitude, page, size);
        return ResponseEntity.ok(venues);
    }
}
