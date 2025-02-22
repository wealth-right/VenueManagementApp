package com.venue.mgmt.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration class for Swagger/OpenAPI documentation.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Configures the OpenAPI documentation for the Venue Management application.
     *
     * @return Configured OpenAPI instance
     */
    @Bean
    public OpenAPI venueManagementOpenAPI() {
        // Configure production server
//        Server productionServer = new Server()
//                .url("https://34.8.162.251/")
//                .description("Production Server");

        // Configure local development server
        Server localServer = new Server()
                .url("http://localhost:8081/api")
                .description("Local Server");

        // Define security scheme name
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Configure servers
                .servers(List.of(localServer))

                // Add security requirement
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))

                // Configure security scheme
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                )

                // Add API information
                .info(new Info()
                        .title("Venue Management API")
                        .version("1.0")
                        .description("API for managing venue leads and registrations")
                        .contact(new Contact()
                                .name("Venue Management Team")
                                .email("support@venue-mgmt.com"))
                        .license(new License()
                                .name("API License")
                                .url("https://www.venue-mgmt.com/licenses"))
                );
    }
}