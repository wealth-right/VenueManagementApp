package com.venue.mgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Optional;

@SpringBootApplication
@RestControllerAdvice
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class VenueManagementAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(VenueManagementAppApplication.class, args);
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of("customer"); // You can modify this to get the actual logged-in user
    }
}
