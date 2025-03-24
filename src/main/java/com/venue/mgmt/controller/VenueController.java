package com.venue.mgmt.controller;

import com.venue.mgmt.constant.GeneralMsgConstants;
import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.response.ApiResponse;
import com.venue.mgmt.response.PaginationDetails;
import com.venue.mgmt.response.VenueResponse;
import com.venue.mgmt.services.VenueService;
import com.venue.mgmt.util.JWTValidator;
import com.venue.mgmt.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.venue.mgmt.constant.GeneralMsgConstants.TOKEN_EXPIRED;

@RestController
@RequestMapping("/venue-app/v1/venues")
public class VenueController {

    private static final Logger logger = LogManager.getLogger(VenueController.class);

    private final VenueService venueService;

    private HttpServletRequest request;


    public VenueController(VenueService venueService,HttpServletRequest request) {
        this.venueService = venueService;
        this.request = request;
    }

    @PostMapping
    public ResponseEntity<VenueResponse<Venue>> createVenue(
            @RequestHeader(name = "Authorization") String authHeader,
            @Valid @RequestBody Venue venue) throws Exception {

        logger.info("VenueManagementApp - Inside create Venue Method");
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if(tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                logger.warn(TOKEN_EXPIRED);
                return ResponseEntity.status(401).build();
            }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            request.setAttribute("userId", userId);
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
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Venue>>> getAllVenues(
            @RequestHeader(name = "Authorization") String authHeader,
            @RequestParam(defaultValue = "desc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws Exception {
        logger.info("VenueManagementApp - Inside get All Venues Method");
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
        boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
        if (isTokenExpired) {
            logger.warn(TOKEN_EXPIRED);
            return ResponseEntity.status(401).build();
        }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            request.setAttribute("userId", userId);
            Page<Venue> venues = venueService.getAllVenuesSortedByCreationDate(sort, page, size, userId);
            venues.forEach(venue -> {
                venue.setLeadCount(venue.getLeads().size());
                venue.setLeadCountToday(venue.getLeadCountToday());
            });
            ResponseEntity<Page<Venue>> responseEntity = ResponseEntity.ok(venues);
            ApiResponse<Page<Venue>> response = new ApiResponse<>();
            response.setStatusCode(responseEntity.getStatusCode().value());
            response.setStatusMsg("Success");
            response.setErrorMsg(null);
            response.setResponse(venues.getContent());

            PaginationDetails paginationDetails = new PaginationDetails();
            paginationDetails.setCurrentPage(venues.getNumber());
            paginationDetails.setTotalRecords(venues.getTotalElements());
            paginationDetails.setTotalPages(venues.getTotalPages());
            response.setPagination(paginationDetails);

            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Venue>>> searchVenues(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(required = false) String query) throws Exception {
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                logger.warn(TOKEN_EXPIRED);
                return ResponseEntity.status(401).build();
            }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            try {
                List<Venue> venues = venueService.searchVenues(query, userId);
                venues.forEach(venue -> {
                    venue.setLeadCount(venue.getLeads().size());
                    venue.setLeadCountToday(venue.getLeadCountToday());
                });
                ResponseEntity<List<Venue>> responseEntity = ResponseEntity.ok(venues);
                ApiResponse<List<Venue>> response = new ApiResponse<>();
                response.setStatusCode(responseEntity.getStatusCode().value());
                response.setStatusMsg("Success");
                response.setErrorMsg(null);
                response.setResponse(venues);
                return ResponseEntity.ok(response);
            }catch (Exception e){
                ApiResponse<List<Venue>> response = new ApiResponse<>();
                response.setStatusCode(500);
                response.setStatusMsg("Failed");
                response.setErrorMsg(e.getMessage());
                response.setResponse(null);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<Venue>>> getVenueDetailsByIds(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(name = "venueIds") List<Long> venueIds) throws Exception {
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                logger.warn(TOKEN_EXPIRED);
                return ResponseEntity.status(401).build();
            }
            try {
                List<Venue> venues = venueService.getVenuesByIds(venueIds);
                venues.forEach(venue -> {
                    venue.setLeadCount(venue.getLeads().size());
                    venue.setLeadCountToday(venue.getLeadCountToday());
                });
                ApiResponse<List<Venue>> response = new ApiResponse<>();
                response.setStatusCode(200);
                response.setStatusMsg("Success");
                response.setErrorMsg(null);
                response.setResponse(venues);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                ApiResponse<List<Venue>> response = new ApiResponse<>();
                response.setStatusCode(500);
                response.setStatusMsg("Failed");
                response.setErrorMsg(e.getMessage());
                response.setResponse(null);
                return ResponseEntity.ok(response);
            }
        }
        return ResponseEntity.status(401).build();
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
