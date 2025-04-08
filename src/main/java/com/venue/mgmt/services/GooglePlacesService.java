package com.venue.mgmt.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.venue.mgmt.dto.VenueDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class GooglePlacesService {


    @Value("${google.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GooglePlacesService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }



    public List<VenueDTO> textSearch(String query, String location, Integer radius, StringBuilder nextPageToken) throws Exception {
        String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
        StringBuilder urlBuilder = new StringBuilder(String.format("https://maps.googleapis.com/maps/api/place/textsearch/json?query=%s&key=%s", encodedQuery, apiKey));

        if (location != null && !location.isEmpty()) {
            urlBuilder.append("&location=").append(location);
        }
        if (radius != null) {
            urlBuilder.append("&radius=").append(radius);
        }

        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(urlBuilder.toString(), HttpMethod.GET, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode results = root.path("results");
        nextPageToken.append(root.path("next_page_token").asText());

        List<VenueDTO> venues = new ArrayList<>();
        if (results.isArray()) {
            for (JsonNode result : results) {
                VenueDTO venue = new VenueDTO();
                venue.setVenueName(result.path("name").asText());
                venue.setAddress(result.path("formatted_address").asText());
                venue.setLatitude(result.path("geometry").path("location").path("lat").asDouble());
                venue.setLongitude(result.path("geometry").path("location").path("lng").asDouble());
                venues.add(venue);
            }
        }
        return venues;
    }



    public JsonNode geocodeAddress(String address) throws Exception {
        String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
        String url = String.format("https://maps.googleapis.com/maps/api/geocode/json?address=%s&key=%s", encodedAddress, apiKey);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(response.getBody());
    }

}

