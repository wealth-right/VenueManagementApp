package com.venue.mgmt.controller;

import com.venue.mgmt.entities.LeadRegistration;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.response.ApiResponse;
import com.venue.mgmt.services.VenueService;
import com.venue.mgmt.util.JWTValidator;
import com.venue.mgmt.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/venues")
public class VenueController {

    private static final Logger logger = LogManager.getLogger(VenueController.class);

    @Autowired
    private VenueService venueService;

    @Autowired
    private HttpServletRequest request;

    @PostMapping
    public ResponseEntity<Venue> createVenue(
            @RequestHeader(name = "Authorization") String authHeader,
            @Valid @RequestBody Venue venue) {

        logger.info("VenueManagementApp - Inside create Venue Method");
        boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
        if (isTokenExpired) {
            logger.warn("Token is expired");
            return ResponseEntity.status(401).build();
        }
        String userId = JwtUtil.extractUserIdFromToken(authHeader);
        request.setAttribute("userId", userId);
        venue.setCreatedBy(userId);
        return ResponseEntity.ok(venueService.saveVenue(venue));
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
            logger.warn("Token is expired");
            return ResponseEntity.status(401).build();
        }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            request.setAttribute("userId", userId);
            Page<Venue> venues = venueService.getAllVenuesSortedByCreationDate(sort, page, size, userId);
            ResponseEntity<Page<Venue>> responseEntity = ResponseEntity.ok(venues);
            ApiResponse<Page<Venue>> response = new ApiResponse<>();
            response.setStatusCode(responseEntity.getStatusCode().value());
            response.setStatusMsg("Success");
            response.setErrorMsg(null);
            response.setResponse(venues.getContent());
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(401).build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Venue>> searchVenues(
            @RequestHeader(name = "Authorization", required = true) String authHeader,
            @RequestParam(required = false) String query) throws Exception {
        boolean tokenValid = JWTValidator.validateToken(authHeader);
        if (tokenValid) {
            boolean isTokenExpired = JwtUtil.checkIfAuthTokenExpired(authHeader);
            if (isTokenExpired) {
                logger.warn("Token is expired");
                return ResponseEntity.status(401).build();
            }
            String userId = JwtUtil.extractUserIdFromToken(authHeader);
            return ResponseEntity.ok(venueService.searchVenues(query, userId));
        }
        return ResponseEntity.status(401).build();
    }

//    @PutMapping("/{venueId}")
//    public ResponseEntity<Venue> updateVenue(
//            @RequestHeader(name = "Authorization") String authHeader,
//            @PathVariable Long venueId,
//            @Valid @RequestBody Venue venue) {
//        return ResponseEntity.ok(venueService.updateVenue(venueId, venue));
//    }

//    @DeleteMapping("/{venueId}")
//    public ResponseEntity<Void> deleteVenue(
//            @RequestHeader(name = "Authorization") String authHeader,
//            @PathVariable Long venueId) {
//        venueService.deleteVenue(venueId);
//        return ResponseEntity.ok().build();
//    }

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
        String userId = request.getUserPrincipal().getName();
        Page<Venue> venues = venueService.getAllVenuesSorted(sortBy, sortDirection, latitude, longitude, page, size);
        return ResponseEntity.ok(venues);
    }
}
