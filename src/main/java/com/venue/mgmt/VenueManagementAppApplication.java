package com.venue.mgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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
}
