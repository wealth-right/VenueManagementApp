package com.venue.mgmt.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class JwtConfig {

    @Value("${jwt.header:Authorization}")
    private String authHeader;

    public String getAuthHeader() {
        return authHeader;
    }
}
