# Use a base image with OpenJDK 17
FROM eclipse-temurin:17-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the Maven project files to the container
COPY target/VenueManagementApp-0.0.1-SNAPSHOT.jar /app/app.jar

# Copy application yml
COPY src/main/resources/application.yml /app/application.yml


# Expose the port your Spring Boot application runs on
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application.yml"]