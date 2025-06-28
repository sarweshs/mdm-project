package com.mdm.globalrules;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Main Spring Boot application class for the MDM Global Rules service.
 * This service manages global merge rules and company-specific overrides.
 */
@SpringBootApplication
@EnableJpaAuditing // Enables JPA auditing for created/updated timestamps
public class MdmGlobalRulesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdmGlobalRulesApplication.class, args);
    }

}