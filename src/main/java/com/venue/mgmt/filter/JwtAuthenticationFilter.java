package com.venue.mgmt.filter;

import com.venue.mgmt.util.JWTValidator;
import com.venue.mgmt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader == null || !JWTValidator.validateToken(authHeader)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (JwtUtil.checkIfAuthTokenExpired(authHeader)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String userId = JwtUtil.extractUserIdFromToken(authHeader);
        request.setAttribute("userId", userId);

        chain.doFilter(request, response);
    }
}