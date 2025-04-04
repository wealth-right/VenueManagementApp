package com.venue.mgmt.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.venue.mgmt.entities.Venue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GooglePlacesService {


    @Value("${google.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public GooglePlacesService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Venue findPlaceFromText(String input) throws JsonProcessingException {
        String url = String.format("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=%s&inputtype=textquery&fields=formatted_address,name,rating,opening_hours,geometry&key=%s", input, apiKey);
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response.getBody());
        JsonNode candidates = root.path("candidates").get(0);
        String address = null;
        double latitude = 0.0;
        double longitude = 0.0;
        if (candidates!=null && (!candidates.isEmpty())) {
            address = candidates.path("formatted_address").asText();
            latitude = candidates.path("geometry").path("location").path("lat").asDouble();
            longitude = candidates.path("geometry").path("location").path("lng").asDouble();
        }
        Venue venue = new Venue();
        venue.setAddress(address);
        venue.setLatitude(latitude);
        venue.setLongitude(longitude);
        venue.setVenueName(input);
        return venue;
    }
}

