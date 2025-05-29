package com.venue.mgmt.request;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class CustomerDetailsClient {


    private static final Logger logger = LogManager.getLogger(CustomerDetailsClient.class);

    private final JdbcTemplate jdbcTemplate;

    public CustomerDetailsClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getCustomerId(String mobileNumber){
        if( mobileNumber == null || mobileNumber.isEmpty()) {
            return null;
        }
        try {
            String sql = "select customerid from customerservice.customer where mobileno = ?";
            return jdbcTemplate.queryForObject(sql, new Object[]{mobileNumber},String.class);
        }catch (Exception e) {
            logger.error("Error while fetching customer ID for mobile number: {}", mobileNumber, e);
            return null;
        }
    }
}
