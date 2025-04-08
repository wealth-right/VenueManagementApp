package com.venue.mgmt;

import com.venue.mgmt.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Optional;

@SpringBootApplication
@RestControllerAdvice
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableWebMvc
public class VenueManagementAppApplication {


    public static void main(String[] args) {
        SpringApplication.run(VenueManagementAppApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();
                    String userId = (String) request.getAttribute("userId");
                    if (userId != null && !userId.trim().isEmpty()) {
                        return Optional.of(userId);
                    }
                }
            } catch (Exception e) {
                // Log error if needed
            }
            return Optional.of("system"); // Default value if no user found
        };
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new JwtAuthenticationFilter());
        registrationBean.addUrlPatterns("/venue-app/v1/*"); // Adjust the URL patterns as needed
        registrationBean.addInitParameter("excludedUrls",
                "/api/venue-app/v1/auth/sendOtp,/api/venue-app/v1/auth/verify-otp");
        return registrationBean;
    }
}
