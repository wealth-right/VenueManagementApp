package com.venue.mgmt.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.venue.mgmt.dto.CustomerAddressDTO;
import com.venue.mgmt.dto.UserDetailsResponse;
import com.venue.mgmt.entities.AddressDetailsEntity;
import com.venue.mgmt.exception.CustomerNotSavedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerServiceClient {

    private static final String ADDRESS_DETAILS_URL = "https://test-devapi.wealth-right.com/Customer/api/customers/address";

    private static final String PERMANENT_ADDRESS = "Permanent";
    private final RestTemplate restTemplate;

    private static final Logger logger = LogManager.getLogger(CustomerServiceClient.class);

    private static final String AUTHORIZATION = "Authorization";

    String saveCustomerUrl="https://test-devapi.wealth-right.com/Customer/api/CreateAndUpdateProspect";

    String deleteCustomerUrl="https://test-devapi.wealth-right.com/Customer/api/Deletecustomer/";
    String getUserDetailsUrl="https://test-devapi.wealth-right.com/Usermgt/api/GetUserDetails/";

    public CustomerServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> saveCustomerData(CustomerRequest customerRequest,String authorization) {
        String url = saveCustomerUrl;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization",  authorization);
        headers.set("Content-Type", "application/json");
        HttpEntity<CustomerRequest> request = new HttpEntity<>(customerRequest, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new CustomerNotSavedException("Failed to save customer data");
        }
        return response;
    }

    public void deleteCustomer(String customerId,String authHeader) {
        String url = deleteCustomerUrl + customerId;
        HttpHeaders headers = new HttpHeaders();
        // Add any necessary headers here, e.g., authorization headers
        headers.set("Authorization",  authHeader);
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, entity, String.class);
        if (response.getStatusCode().is2xxSuccessful()) {
            logger.info("Successfully deleted customer with ID: {}", customerId);
        } else {
            logger.error("Failed to delete customer with ID: {}. Status code: {}", customerId, response.getStatusCode());
        }
    }

    public void addCustomerAddress(
            String customerId,
            boolean isCommunicationAddSame,
            AddressDetailsEntity addressDetails,
            String createdBy,
            String autHeader) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("accept", "text/plain");
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(AUTHORIZATION, autHeader);

        // Map AddressDetailsEntity to DTO
        List<CustomerAddressDTO> addressList =
                addressDetails != null
                        ? List.of(mapToCustomerAddressDTO(addressDetails, createdBy))
                        : Collections.emptyList();

        if (addressList.isEmpty()) {
            logger.warn("No address details found for customer ID: {}. Skipping address API call.", customerId);
            return;
        }

        // Convert DTOs to map structure
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> addressListMap =
                addressList.stream()
                        .map(dto -> objectMapper.convertValue(dto, new TypeReference<Map<String, Object>>() {}))
                        .toList();

        // Construct request body matching the API (no wrapper key)
        Map<String, Object> payload = new HashMap<>();
        payload.put("customerId", customerId);
        payload.put("isCommunicationAddSame", isCommunicationAddSame);
        payload.put("customerAddresses", addressListMap);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        logger.info(
                "Sending request to add customer address for customer ID: {} with payload: {}",
                customerId,
                payload);

        try {
            // Send the POST request
            ResponseEntity<String> response =
                    restTemplate.exchange(ADDRESS_DETAILS_URL, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                logger.error(
                        "Failed to add customer address for customer ID: {}. Status code: {}",
                        customerId,
                        response.getStatusCode());
                throw new CustomerNotSavedException("Failed to add customer address");
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String responseBody = ex.getResponseBodyAsString();
            String errorMsg =
                    extractErrorMessage(responseBody); // Utility method to parse meaningful message
            logger.error(
                    "Failed to add customer address for customer ID: {}. Error: {}", customerId, errorMsg);
            throw new CustomerNotSavedException("Failed to add customer address: " + errorMsg);
        } catch (Exception ex) {
            logger.error(
                    "Failed to add customer address for customer ID: {} due to internal error",
                    customerId,
                    ex);
            throw new CustomerNotSavedException("Failed to add customer address due to internal error");
        }
        logger.info("Successfully added customer address for customer ID: {}", customerId);
    }


    private CustomerAddressDTO mapToCustomerAddressDTO(
            AddressDetailsEntity addressDetail, String createdBy) {
        CustomerAddressDTO dto = new CustomerAddressDTO();
        dto.setAddressType(PERMANENT_ADDRESS);
        dto.setAddressLine1(addressDetail.getPermanentAddressLine1());
        //    hard-coded changes this needs to be updated as per feedbacks in future.
        dto.setAddressLine2(addressDetail.getPermanentAddressLine2()!= null
                ? addressDetail.getPermanentAddressLine2()
                : " ");
        dto.setAddressLine3(addressDetail.getCommunicationAddressLine1()!= null
                ? addressDetail.getCommunicationAddressLine1()
                : " ");
        dto.setAddressLine4(addressDetail.getCommunicationAddressLine2()!= null
                ? addressDetail.getCommunicationAddressLine2()
                : " ");
        dto.setAddressLine5(" ");
        dto.setCityName(addressDetail.getPermanentCity());
        dto.setStateName(addressDetail.getPermanentState());
        dto.setStatus(true);
        dto.setCountryName(addressDetail.getPermanentCountry());
        dto.setPinCode(addressDetail.getPermanentPincode());
        dto.setCreatedBy(createdBy);
        return dto;
    }

    private String extractErrorMessage(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(responseBody);
            JsonNode errorNode = root.path("errorMsg");
            if (!errorNode.isMissingNode()) {
                return errorNode.asText();
            } else {
                return "Unknown error occurred while saving customer";
            }
        } catch (Exception e) {
            return "Unable to parse error message from response";
        }
    }

}