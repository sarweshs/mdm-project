package com.mdm.botcore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot application class for the MDM Bot Core service.
 * This service houses the Drools rule engine, core merge logic, and audit logging.
 */
@SpringBootApplication
@EnableJpaAuditing // Enables JPA auditing for created/updated timestamps
public class MdmBotCoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdmBotCoreApplication.class, args);
    }
}