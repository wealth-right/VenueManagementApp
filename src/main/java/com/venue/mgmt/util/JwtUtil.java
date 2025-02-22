package com.venue.mgmt.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.venue.mgmt.controller.LeadRegistrationController;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LogManager.getLogger(JwtUtil.class);

    private static final String DEFAULT_USER = "system_user"; // Default user for development

    @Value("${jwt.secret}")
    private String secret;

    public static boolean checkIfAuthTokenExpired(String authHeader) {
        if ((authHeader != null || !authHeader.trim().isEmpty()) && authHeader.startsWith("Bearer ")) {
            authHeader = authHeader.substring(7);
        }
        DecodedJWT decodedJWT = JWT.decode(authHeader);
        return decodedJWT.getExpiresAt().before(new Date());
    }

    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String extractUserIdFromToken(String token) {
        logger.info("Received token: {}", token);

        if (token == null || token.trim().isEmpty()) {
            logger.info("No token provided, using default user");
            return DEFAULT_USER;
        }

        try {
            // Remove 'Bearer ' if present
            if (token.startsWith("Bearer ")) {
                logger.info("Found Bearer prefix, removing it");
                token = token.substring(7);
            }
            DecodedJWT decodedJWT = JWT.decode(token);

            logger.info("User Id: {}", decodedJWT.getClaims().get("id"));
            Claim id = decodedJWT.getClaims().get("id");

            return id.asString();

        } catch (Exception e) {
            logger.error("Error extracting userId from token: {}", e.getMessage(), e);
            return DEFAULT_USER;
        }
    }

}
