package com.venue.mgmt.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class AddressDetailsService {

    private static final Logger logger = LogManager.getLogger(AddressDetailsService.class);
    private static final String ADDRESS_URL = "http://localhost:8080/api/lmsapi/api/v1/public/quick-tap/api/lms/save-address";

    public void setAddressDetails(Long leadId,String pinCode,String address,String authHeader) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("leadId", String.valueOf(leadId));
            params.add("fullAddress", address); // assumes address is string
            params.add("pinCode", pinCode);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Bearer " + authHeader);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> addressResponse = restTemplate.postForEntity(ADDRESS_URL, requestEntity, String.class);

            logger.info("Address save response: {}", addressResponse.getBody());
        } catch (Exception e) {
            logger.error("Failed to save address: ", e);
            // Optionally handle rollback or just log and proceed
        }

    }

}
