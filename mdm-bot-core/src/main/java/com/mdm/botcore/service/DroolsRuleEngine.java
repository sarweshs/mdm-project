package com.mdm.botcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdm.botcore.config.DroolsConfig;
import com.mdm.botcore.domain.model.MDMEntity;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.ArrayList;
import java.util.List;

@Component
@Qualifier("droolsRuleEngine")
@Scope("prototype")
public class DroolsRuleEngine implements RuleEngine {
    private final ApplicationContext applicationContext;
    private final ObjectMapper objectMapper;

    @Autowired
    public DroolsRuleEngine(ApplicationContext applicationContext, ObjectMapper objectMapper) {
        this.applicationContext = applicationContext;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<MergeService.MergeSuggestion> processEntities(List<MDMEntity> entities, List<String> rules) {
        List<MergeService.MergeSuggestion> mergeSuggestions = new ArrayList<>();
        if (entities == null || entities.isEmpty() || rules == null || rules.isEmpty()) {
            return mergeSuggestions;
        }
        KieContainer kieContainer = applicationContext.getBean(DroolsConfig.class).kieContainer(rules);
        KieSession kieSession = kieContainer.newKieSession();
        try {
            kieSession.setGlobal("mergeSuggestions", mergeSuggestions);
            kieSession.setGlobal("objectMapper", objectMapper);
            entities.forEach(kieSession::insert);
            kieSession.fireAllRules();
        } finally {
            kieSession.dispose();
        }
        return mergeSuggestions;
    }
} 