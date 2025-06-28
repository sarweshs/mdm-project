package com.mdm.botcore.config;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.io.StringReader;
import java.util.List;

/**
 * Configuration class for setting up the Drools KieContainer and KieSession.
 * This class is responsible for compiling DRL rules dynamically.
 */
@Configuration
public class DroolsConfig {

    private KieServices kieServices = KieServices.Factory.get();

    /**
     * Creates and returns a KieFileSystem bean.
     * KieFileSystem is used to manage the resources (DRL files) that compose the KieBase.
     * @return KieFileSystem instance.
     */
    @Bean
    public KieFileSystem kieFileSystem() {
        return kieServices.newKieFileSystem();
    }

    /**
     * Creates and returns a KieContainer bean.
     * The KieContainer holds the KieBase (compiled rules) and provides a way to get KieSessions.
     * This method dynamically builds the KieContainer from the provided DRL strings.
     *
     * @param drlRules A list of DRL rule strings obtained from the rule management service.
     * @return A KieContainer with the compiled rules.
     */
    @Bean
    @Scope("prototype") // New KieContainer is created each time this bean is requested with rules
    public KieContainer kieContainer(List<String> drlRules) {
        KieRepository kieRepository = kieServices.getRepository();

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

        // Add each DRL rule string as a resource to the KieFileSystem
        for (int i = 0; i < drlRules.size(); i++) {
            String drl = drlRules.get(i);
            // Provide a unique path for each rule.
            // Using a generic package name 'com.mdm.rules' helps organize rules.
            kieFileSystem.write(ResourceFactory.newReaderResource(new StringReader(drl))
                    .setSourcePath("src/main/resources/com/mdm/rules/dynamic_rule_" + i + ".drl"));
        }

        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll(); // Compile all rules

        if (kieBuilder.getResults().hasMessages(org.kie.api.builder.Message.Level.ERROR)) {
            throw new RuntimeException("Error building Drools KieBase: " + kieBuilder.getResults().toString());
        }

        // Return a new KieContainer associated with the default release ID, which now contains our compiled rules.
        return kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
    }

    /**
     * Creates and returns a KieSession bean.
     * A KieSession is the runtime component where facts (MDM entities) are inserted and rules are fired.
     * This bean is typically scoped as 'prototype' or created on demand for each new rule execution.
     *
     * Note: This bean depends on a `KieContainer` being available.
     * When you `autowire` KieSession, Spring will internally resolve `KieContainer` first.
     * If you want a fresh session for each merge process, you'd create it programmatically
     * from the `KieContainer` obtained dynamically, or use `@Scope("prototype")`.
     *
     * For dynamic rule loading from DB, `kieContainer(List<String> drlRules)` will be called
     * by the `MergeService` to get a fresh container with up-to-date rules.
     * So, this `kieSession()` bean might not be directly used for dynamic rules in all cases,
     * but it demonstrates how to get a session.
     *
     * @param kieContainer The KieContainer with compiled rules.
     * @return A new KieSession.
     */
    @Bean
    @Scope("prototype") // Ensures a new session for each injection/request
    public KieSession kieSession(KieContainer kieContainer) {
        return kieContainer.newKieSession();
    }
}