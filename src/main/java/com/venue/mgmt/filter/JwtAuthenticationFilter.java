package com.venue.mgmt.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.venue.mgmt.util.JWTValidator;
import com.venue.mgmt.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthenticationFilter extends HttpFilter {

    private List<String> excludedUrls;

    @Override
    public void init() throws ServletException {
        String excludedUrlsParam = getFilterConfig().getInitParameter("excludedUrls");
        if (StringUtils.hasText(excludedUrlsParam)) {
            excludedUrls = Arrays.asList(excludedUrlsParam.split(","));
        }
    }

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String requestUri = request.getRequestURI();
        if (excludedUrls != null && excludedUrls.stream().anyMatch(requestUri::startsWith)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader == null || !JWTValidator.validateToken(authHeader)) {
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Invalid or missing Authorization Token");
                return;
            }
        } catch (Exception e) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", e.getMessage());
            return;
        }

        if (JwtUtil.checkIfAuthTokenExpired(authHeader)) {
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Token has expired");
            return;
        }
        String userId = JwtUtil.extractUserIdFromToken(authHeader);
        request.setAttribute("userId", userId);

        chain.doFilter(request, response);
    }
    private void sendErrorResponse(HttpServletResponse response, int statusCode, String statusMsg, String errorMsg) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("statusCode", statusCode);
        responseBody.put("statusMsg", statusMsg);
        responseBody.put("errorMsg", errorMsg);
        responseBody.put("response", null);
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}