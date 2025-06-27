package com.venue.mgmt.controller;

import com.venue.mgmt.constant.ErrorMsgConstants;
import com.venue.mgmt.dto.VenueDTO;
import com.venue.mgmt.entities.Venue;
import com.venue.mgmt.exception.VenueAlreadyExistsException;
import com.venue.mgmt.repositories.LeadRegRepository;
import com.venue.mgmt.request.UserMasterRequest;
import com.venue.mgmt.response.*;
import com.venue.mgmt.services.GooglePlacesService;
import com.venue.mgmt.services.UserMgmtResService;
import com.venue.mgmt.services.VenueFacadeService;
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

import static com.venue.mgmt.constant.GeneralMsgConstants.*;

@RestController
@RequestMapping("/venue-app/v1/venues")
public class VenueController {

    private static final Logger logger = LogManager.getLogger(VenueController.class);

    private final VenueService venueService;

    private final HttpServletRequest request;

    private final LeadRegRepository leadRegRepository;

    private final GooglePlacesService googleMapsService;

    private final VenueFacadeService venueFacadeService;
    private final UserMgmtResService userMgmtResService;


    public VenueController(VenueService venueService, HttpServletRequest request,LeadRegRepository leadRegRepository,
                           GooglePlacesService googleMapsService,
                           VenueFacadeService venueFacadeService, UserMgmtResService userMgmtResService) {
        this.venueService = venueService;
        this.request = request;
        this.leadRegRepository = leadRegRepository;
        this.googleMapsService = googleMapsService;
        this.venueFacadeService = venueFacadeService;
        this.userMgmtResService = userMgmtResService;
    }

    @PostMapping
    public ResponseEntity<VenueResponse<Venue>> createVenue(
            @Valid @RequestBody Venue venue) throws VenueAlreadyExistsException {
        logger.info("VenueManagementApp - Inside create Venue Method");

        String userId = (String) request.getAttribute(USER_ID);
        try {
            venueFacadeService.fetchAndSetAddressDetails(venue);
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
            if (nextPageToken != null && !nextPageToken.isEmpty()) {
                searchResult = googleMapsService.textSearchWithToken(nextPageToken, nextPageTokenBuilder);
            } else {
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
            @RequestParam(value = "location", required = false) String location,
            @PageableDefault(sort = "created_at", direction = Sort.Direction.DESC, page = 1, size = 20) Pageable pageable) {
        logger.info("VenueManagementApp - Inside get All Venues Method");
        try {
            String userId = (String) request.getAttribute(USER_ID);
            UserMasterRequest userMasterDetails = userMgmtResService.getUserMasterDetails(userId);
//            extract the channelCode from the userMasterDetails
            String channelCode = userMasterDetails.getChannelCode();
            pageable = PageRequest.of(pageable.getPageNumber() - 1, pageable.getPageSize(), pageable.getSort());
            Page<Venue> venues = venueFacadeService.getVenuesByLocationOrDefault(location, channelCode, pageable);
            venueFacadeService.calculateTotalLeadsCount(venues.getContent(), userId, leadRegRepository);
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
           venueFacadeService.calculateTotalLeadsCount(venues, userId, leadRegRepository);
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
            venueFacadeService.calculateTotalLeadsCount(venues, userId, leadRegRepository);
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
            @RequestParam(defaultValue = "created_at") String sortBy,
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

           venueFacadeService.fetchAndSetAddressDetails(venue);
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
