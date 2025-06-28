package com.mdm.reviewdashboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Main Spring Boot application class for the MDM Review Dashboard service.
 * This service provides a web interface for human agents to review and manage merge candidates.
 */
@SpringBootApplication
public class MdmReviewDashboardApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdmReviewDashboardApplication.class, args);
    }

    /**
     * Configures a WebClient bean for making HTTP requests to other microservices.
     * @param builder WebClient.Builder provided by Spring.
     * @return A configured WebClient instance.
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // This builder can be further configured (e.g., base URL for common services)
        return builder.build();
    }
}