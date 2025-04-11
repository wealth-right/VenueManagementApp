package com.venue.mgmt.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.venue.mgmt.constant.ErrorMsgConstants;
import static com.venue.mgmt.constant.GeneralMsgConstants.*;
import com.venue.mgmt.dto.VenueDTO;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.response.*;
import com.venue.mgmt.services.GooglePlacesService;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/venue-app/v1/venues")
public class VenueController {

    private static final Logger logger = LogManager.getLogger(VenueController.class);

    private final VenueService venueService;

    private final HttpServletRequest request;

    private final LeadRegRepository leadRegRepository;

    private final GooglePlacesService googleMapsService;


    public VenueController(VenueService venueService, HttpServletRequest request,LeadRegRepository leadRegRepository,
                           GooglePlacesService googleMapsService) {
        this.venueService = venueService;
        this.request = request;
        this.leadRegRepository = leadRegRepository;
        this.googleMapsService = googleMapsService;
    }

    @PostMapping
    public ResponseEntity<VenueResponse<Venue>> createVenue(
            @Valid @RequestBody Venue venue) {

        String userId = (String) request.getAttribute(USER_ID);
        try {
                JsonNode geocodeResponse = googleMapsService.geocodeAddress(venue.getAddress());
            if (geocodeResponse == null || !geocodeResponse.has("results") || geocodeResponse.path("results").isEmpty()) {
                VenueResponse<Venue> response = new VenueResponse<>();
                response.setStatusCode(400);
                response.setStatusMsg(ErrorMsgConstants.INVALID_ADDRESS);
                response.setErrorMsg(ErrorMsgConstants.GEOCODE_ERROR);
                response.setResponse(null);
                return ResponseEntity.badRequest().body(response);
            }
            JsonNode addressComponents = geocodeResponse.path("results").get(0).path("address_components");
                for (JsonNode component : addressComponents) {
                    List<String> types = new ArrayList<>();
                    component.path("types").forEach(type -> types.add(type.asText()));
                    if (types.contains(POSTAL_CODE)) {
                        venue.setPinCode(component.path(LONG_NAME).asText());
                    }
                    if (types.contains(LOCALITY)) {
                        venue.setCity(component.path(LONG_NAME).asText());
                    }
                    if( types.contains(SUB_LOCALITY)) {
                        venue.setLocality(component.path(LONG_NAME).asText());
                    }
                    if (types.contains(STATE)) {
                      venue.setState(component.path(LONG_NAME).asText());
                    }
                    if (types.contains(COUNTRY)) {
                        venue.setCountry(component.path(LONG_NAME).asText());
                    }
                }
            venue.setCreatedBy(userId);
            Venue savedVenue = venueService.saveVenue(venue);
            VenueResponse<Venue> response = new VenueResponse<>();
            response.setStatusCode(OK);
            response.setStatusMsg(SUCCESS);
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

    @GetMapping("/text-search")
    public ResponseEntity<GoogleMapResponse<VenueSearchResponse>> textSearch(
            @RequestParam String query,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer radius,
            @RequestParam(required = false) String nextPageToken) {
        try {
            StringBuilder nextPageTokenBuilder = new StringBuilder();
            List<VenueDTO> searchResult;
            // If nextPageToken is provided, fetch the next set of results
            if (nextPageToken != null && !nextPageToken.isEmpty()) {
                searchResult = googleMapsService.textSearchWithToken(nextPageToken, nextPageTokenBuilder);
            } else {
                // Otherwise, perform a new search
                searchResult = googleMapsService.textSearch(query, location, radius, nextPageTokenBuilder);
            }
            VenueSearchResponse venueSearchResponse = new VenueSearchResponse();
            venueSearchResponse.setVenues(searchResult);
            venueSearchResponse.setNextPageToken(nextPageTokenBuilder.toString());
            GoogleMapResponse<VenueSearchResponse> response = new GoogleMapResponse<>();
            response.setStatusCode(OK);
            response.setStatusMsg(SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(venueSearchResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            GoogleMapResponse<VenueSearchResponse> response = new GoogleMapResponse<>();
            response.setStatusCode(500);
            response.setStatusMsg("Error while performing text search");
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.internalServerError().body(response);
        }
    }


    @GetMapping
    public ResponseEntity<ApiResponse<Page<Venue>>> getAllVenues(
            @PageableDefault(sort = "creationDate", direction = Sort.Direction.DESC, page = 1, size = 20) Pageable pageable) {
        logger.info("VenueManagementApp - Inside get All Venues Method");
        try {
            String userId = (String) request.getAttribute(USER_ID);
            pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());
            Page<Venue> venues = venueService.getAllVenuesSortedByCreationDate(pageable.getSort().toString(),
                    pageable.getPageNumber(), pageable.getPageSize(), userId);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date today = calendar.getTime();
            for (Venue venue : venues) {
                int leadCount = leadRegRepository.countByVenue_VenueIdAndCreatedByAndIsDeletedFalse(
                        venue.getVenueId(), userId);
                // Count today's leads
                int leadCountToday = leadRegRepository.countByVenue_VenueIdAndCreatedByAndCreationDateAndIsDeletedFalse(
                        venue.getVenueId(), userId, today);

                venue.setLeadCount(leadCount);
                venue.setLeadCountToday(leadCountToday);
            }
            ResponseEntity<Page<Venue>> responseEntity = ResponseEntity.ok(venues);
            ApiResponse<Page<Venue>> response = new ApiResponse<>();
            response.setStatusCode(responseEntity.getStatusCode().value());
            response.setStatusMsg(SUCCESS);
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
            @RequestParam(required = false) String query) {
        String userId = (String) request.getAttribute(USER_ID);
        try {
            List<Venue> venues = venueService.searchVenues(query, userId);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date today = calendar.getTime();
            for (Venue venue : venues) {
                int leadCount = leadRegRepository.countByVenue_VenueIdAndCreatedByAndIsDeletedFalse(
                        venue.getVenueId(), userId);
                // Count today's leads
                int leadCountToday = leadRegRepository.countByVenue_VenueIdAndCreatedByAndCreationDateAndIsDeletedFalse(
                        venue.getVenueId(), userId, today);
                venue.setLeadCount(leadCount);
                venue.setLeadCountToday(leadCountToday);
            }
            ResponseEntity<List<Venue>> responseEntity = ResponseEntity.ok(venues);
            ApiResponse<List<Venue>> response = new ApiResponse<>();
            response.setStatusCode(responseEntity.getStatusCode().value());
            response.setStatusMsg(SUCCESS);
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
            @RequestParam(name = "venueIds") List<Long> venueIds) {
        try {
            String userId = (String) request.getAttribute(USER_ID);
            List<Venue> venues = venueService.getVenuesByIds(venueIds);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date today = calendar.getTime();
            for (Venue venue : venues) {
                int leadCount = leadRegRepository.countByVenue_VenueIdAndCreatedByAndIsDeletedFalse(
                        venue.getVenueId(), userId);
                // Count today's leads
                int leadCountToday = leadRegRepository.countByVenue_VenueIdAndCreatedByAndCreationDateAndIsDeletedFalse(
                        venue.getVenueId(), userId, today);
                venue.setLeadCount(leadCount);
                venue.setLeadCountToday(leadCountToday);
            }
            ApiResponse<List<Venue>> response = new ApiResponse<>();
            response.setStatusCode(OK);
            response.setStatusMsg(SUCCESS);
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

    @GetMapping("/sorted")
    public ResponseEntity<Page<Venue>> getAllVenuesSorted(
            @RequestParam(defaultValue = "creationDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Venue> venues = venueService.getAllVenuesSortedByDistance(sortDirection, latitude, longitude, page, size);
        return ResponseEntity.ok(venues);
    }

    @PutMapping("/{venueId}")
    public ResponseEntity<VenueResponse<Venue>> updateVenue(
            @PathVariable Long venueId,
            @Valid @RequestBody Venue venue) {
        try {
            // Call the geocoding API to extract address details
            JsonNode geocodeResponse = googleMapsService.geocodeAddress(venue.getAddress());
            if (geocodeResponse == null || !geocodeResponse.has("results") || geocodeResponse.path("results").isEmpty()) {
                VenueResponse<Venue> response = new VenueResponse<>();
                response.setStatusCode(400);
                response.setStatusMsg(ErrorMsgConstants.INVALID_ADDRESS);
                response.setErrorMsg(ErrorMsgConstants.GEOCODE_ERROR);
                response.setResponse(null);
                return ResponseEntity.badRequest().body(response);
            }
            JsonNode addressComponents = geocodeResponse.path("results").get(0).path("address_components");
            for (JsonNode component : addressComponents) {
                List<String> types = new ArrayList<>();
                component.path("types").forEach(type -> types.add(type.asText()));
                if (types.contains(POSTAL_CODE)) {
                    venue.setPinCode(component.path(LONG_NAME).asText());
                }
                if (types.contains(LOCALITY)) {
                    venue.setCity(component.path(LONG_NAME).asText());
                }
                if (types.contains(SUB_LOCALITY)) {
                    venue.setLocality(component.path(LONG_NAME).asText());
                }
                if (types.contains(STATE)) {
                    venue.setState(component.path(LONG_NAME).asText());
                }
                if (types.contains(COUNTRY)) {
                    venue.setCountry(component.path(LONG_NAME).asText());
                }
            }
            // Update the venue in the database
            Venue updatedVenue = venueService.updateVenue(venueId, venue);
            VenueResponse<Venue> response = new VenueResponse<>();
            response.setStatusCode(OK);
            response.setStatusMsg(SUCCESS);
            response.setErrorMsg(null);
            response.setResponse(updatedVenue);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            VenueResponse<Venue> response = new VenueResponse<>();
            response.setStatusCode(500);
            response.setStatusMsg(ErrorMsgConstants.FAILED);
            response.setErrorMsg(e.getMessage());
            response.setResponse(null);
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
