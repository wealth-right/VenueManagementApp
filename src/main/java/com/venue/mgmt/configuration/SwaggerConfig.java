package com.venue.mgmt.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
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
        Server productionServer = new Server()
                .url("http://34.8.162.251/api")
                .description("Production Server");

        Server uatServer = new Server()
                .url("https://sit-services.wealth-right.com/api/venue")
                .description("UAT Server");

        // Configure local development server
        Server localServer = new Server()
                .url("http://localhost:8080/api/venue")
                .description("Local Server");

        Server devServer = new Server()
                .url("http://localhost:8081/api")
                .description("Dev Server");

        Server finalProductionServer= new Server()
                .url("https://edge.wealth-right.com/api/quicktap")
                .description("Final Production Server");

        // Define security scheme name
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Configure servers
                .servers(List.of(localServer,productionServer,uatServer,devServer,finalProductionServer))

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
                );

                // Add API information
    }
}