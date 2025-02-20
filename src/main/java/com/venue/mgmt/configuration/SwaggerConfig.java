package com.venue.mgmt.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI venueManagementOpenAPI() {
        Server server = new Server()
            .url("http://localhost:8081")
            .description("Local Development Server");

        return new OpenAPI()
                .servers(List.of(server))
                .info(new Info()
                        .title("Venue Management API Documentation")
                        .description("API documentation for Venue Management Application")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Venue Management Team")
                                .email("")));
    }
}
