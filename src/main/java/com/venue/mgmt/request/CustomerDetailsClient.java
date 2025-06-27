package com.venue.mgmt.request;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerDetailsClient {


    private static final Logger logger = LogManager.getLogger(CustomerDetailsClient.class);

    private final JdbcTemplate jdbcTemplate;

    public CustomerDetailsClient(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String getCustomerId(String mobileNumber) {
        if (mobileNumber == null || mobileNumber.isEmpty()) {
            return null;
        }
        try {
            String sql = "SELECT customerid FROM customerservice.customer WHERE mobileno = ?";
            List<String> results = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> rs.getString("customerid"),
                    mobileNumber
            );
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            logger.error("Error while fetching customer ID for mobile number: {}", mobileNumber, e);
            return null;
        }
    }

}
