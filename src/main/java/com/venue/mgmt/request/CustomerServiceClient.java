package com.venue.mgmt.request;

import com.venue.mgmt.dto.UserDetailsResponse;
import com.venue.mgmt.exception.CustomerNotSavedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class CustomerServiceClient {

    private final RestTemplate restTemplate;

    private static final Logger logger = LogManager.getLogger(CustomerServiceClient.class);

    String saveCustomerUrl="https://api.dev.wealth-right.com/Customer/api/CreateAndUpdateProspect";

    String deleteCustomerUrl="https://api.dev.wealth-right.com/Customer/api/Deletecustomer/";
    String getUserDetailsUrl="https://api.dev.wealth-right.com/Usermgt/api/GetUserDetails/";

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

    public UserDetailsResponse.UserDetails getUserDetails(String userId) {
        String url = getUserDetailsUrl + userId + "/USERID";
        UserDetailsResponse response = restTemplate.getForObject(url, UserDetailsResponse.class);
        return response != null ? response.getResponse() : null;
    }
}