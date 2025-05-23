//package com.venue.mgmt.controller;
//
//import com.venue.mgmt.entities.Venue;
//import com.venue.mgmt.repositories.LeadRegRepository;
//import com.venue.mgmt.repositories.VenueRepository;
//import com.venue.mgmt.response.ApiResponse;
//import com.venue.mgmt.services.VenueService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.domain.*;
//import org.springframework.http.ResponseEntity;
//
//import java.util.Collections;
//import java.util.List;
//
//@ExtendWith(MockitoExtension.class)
//class VenueControllerTest {
//
//    @InjectMocks
//    private VenueController venueController;
//
//    @Mock
//    private VenueService venueService;
//
//    @Mock
//    private HttpServletRequest request;
//
//    @Mock
//    private LeadRegRepository leadRegRepository;
//
//    @Mock
//    private VenueRepository venueRepos;
//
//    private Pageable pageable;
//
//    @BeforeEach
//    void setUp() {
//        pageable = PageRequest.of(0, 20, Sort.by("creationDate").descending());
//    }
//
//    @Test
//    @DisplayName("Test getAllVenues with no location provided")
//    void testGetAllVenues_NoLocationProvided() {
//        String userId = "SOS-997577";
//        Page<Venue> mockVenues = new PageImpl<>(List.of(new Venue()));
//
//        Mockito.when(venueService.getAllVenuesSortedByCreationDate(
//                        pageable.getSort().toString(), pageable.getPageNumber(), pageable.getPageSize(), userId))
//                .thenReturn(mockVenues);
//
//        ResponseEntity<ApiResponse<Page<Venue>>> response = venueController.getAllVenues(null, pageable);
//
//        Assertions.assertEquals(200, response.getStatusCodeValue());
//        Assertions.assertNotNull(response.getBody());
//        Assertions.assertEquals(1, response.getBody().getResponse().size());
//    }
//
//    @Test
//    @DisplayName("Test getAllVenues with valid location provided")
//    void testGetAllVenues_ValidLocationProvided() {
//        String userId = "testUser";
//        List<Venue> mockVenues = List.of(new Venue());
//        Mockito.when(request.getAttribute("USER_ID")).thenReturn(userId);
//        Mockito.when(venueRepos.findAllByCreatedBy(userId)).thenReturn(mockVenues);
//        Mockito.when(venueService.calculateDistance(Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble(), Mockito.anyDouble()))
//                .thenReturn(10.0);
//
//        ResponseEntity<ApiResponse<Page<Venue>>> response = venueController.getAllVenues("12.34,56.78", pageable);
//
//        Assertions.assertEquals(200, response.getStatusCodeValue());
//        Assertions.assertNotNull(response.getBody());
//        Assertions.assertEquals(1, response.getBody().getResponse().size());
//    }
//
//    @Test
//    @DisplayName("Test getAllVenues with invalid location format")
//    void testGetAllVenues_InvalidLocationFormat() {
//        String userId = "testUser";
//        Page<Venue> mockVenues = new PageImpl<>(List.of(new Venue()));
//
//        Mockito.when(request.getAttribute("USER_ID")).thenReturn(userId);
//        Mockito.when(venueService.getAllVenuesSortedByCreationDate(
//                        pageable.getSort().toString(), pageable.getPageNumber(), pageable.getPageSize(), userId))
//                .thenReturn(mockVenues);
//
//        ResponseEntity<ApiResponse<Page<Venue>>> response = venueController.getAllVenues("invalidLocation", pageable);
//
//        Assertions.assertEquals(200, response.getStatusCodeValue());
//        Assertions.assertNotNull(response.getBody());
//        Assertions.assertEquals(1, response.getBody().getResponse().size());
//    }
//
//    @Test
//    @DisplayName("Test getAllVenues with empty location string")
//    void testGetAllVenues_NoVenuesFound() {
//        String userId = "testUser";
//        Page<Venue> mockVenues = new PageImpl<>(Collections.emptyList());
//
//        Mockito.when(request.getAttribute("USER_ID")).thenReturn(userId);
//        Mockito.when(venueService.getAllVenuesSortedByCreationDate(
//                        pageable.getSort().toString(), pageable.getPageNumber(), pageable.getPageSize(), userId))
//                .thenReturn(mockVenues);
//
//        ResponseEntity<ApiResponse<Page<Venue>>> response = venueController.getAllVenues(null, pageable);
//
//        Assertions.assertEquals(200, response.getStatusCodeValue());
//        Assertions.assertNotNull(response.getBody());
//        Assertions.assertTrue(response.getBody().getResponse().isEmpty());
//    }
//
//    @Test
//    @DisplayName("Test getAllVenues when an exception occurs")
//    void testGetAllVenues_ExceptionOccurs() {
//        String userId = "testUser";
//
//        Mockito.when(request.getAttribute("USER_ID")).thenReturn(userId);
//        Mockito.when(venueService.getAllVenuesSortedByCreationDate(
//                        pageable.getSort().toString(), pageable.getPageNumber(), pageable.getPageSize(), userId))
//                .thenThrow(new RuntimeException("Test Exception"));
//
//        ResponseEntity<ApiResponse<Page<Venue>>> response = venueController.getAllVenues(null, pageable);
//
//        Assertions.assertEquals(500, response.getStatusCodeValue());
//        Assertions.assertNotNull(response.getBody());
//        Assertions.assertEquals("Test Exception", response.getBody().getErrorMsg());
//    }
//}