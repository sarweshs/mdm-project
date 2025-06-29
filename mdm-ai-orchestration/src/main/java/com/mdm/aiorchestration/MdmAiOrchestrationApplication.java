package com.mdm.aiorchestration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Main Spring Boot application for the MDM AI Orchestration service.
 * This service mediates communication between the dashboard bot and other MDM services.
 */
@SpringBootApplication(exclude = {HibernateJpaAutoConfiguration.class})
public class MdmAiOrchestrationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdmAiOrchestrationApplication.class, args);
    }

    /**
     * Configures a WebClient bean for making HTTP requests to other microservices.
     * This WebClient will be used by BotService to call mdm-bot-core.
     * @param builder WebClient.Builder provided by Spring.
     * @return A configured WebClient instance.
     */
    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }
}