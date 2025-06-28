package com.mdm.botcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdm.botcore.domain.model.MDMEntity;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class RuleBookRuleEngineTest {

    @Test
    void testProcessEntities_CompanyNameAndPhoneMatch() {
        ObjectMapper objectMapper = new ObjectMapper();
        RuleBookRuleEngine engine = new RuleBookRuleEngine(objectMapper);

        MDMEntity e1 = new MDMEntity();
        e1.setId("1");
        e1.setType("Organization");
        e1.setName("Acme Corporation");
        e1.setPhone("555-123-4567");

        MDMEntity e2 = new MDMEntity();
        e2.setId("2");
        e2.setType("Organization");
        e2.setName("ACME CORPORATION");
        e2.setPhone("555-123-4567");

        MDMEntity e3 = new MDMEntity();
        e3.setId("3");
        e3.setType("Organization");
        e3.setName("Different Company");
        e3.setPhone("555-987-6543");

        List<MDMEntity> entities = Arrays.asList(e1, e2, e3);
        List<String> rules = Collections.emptyList(); // Not used in pure Java impl

        List<MergeService.MergeSuggestion> suggestions = engine.processEntities(entities, rules);

        // Should find at least one company name match and one phone match
        boolean foundCompanyNameMatch = suggestions.stream().anyMatch(s -> "ExactCompanyNameMatch".equals(s.getRuleName()));
        boolean foundPhoneMatch = suggestions.stream().anyMatch(s -> "PhoneNumberMatch".equals(s.getRuleName()));

        assertTrue(foundCompanyNameMatch, "Should find company name match");
        assertTrue(foundPhoneMatch, "Should find phone number match");

        // Should not find a match for e1 and e3 (different name and phone)
        boolean foundWrongMatch = suggestions.stream().anyMatch(s ->
            (s.getEntity1().getId().equals("1") && s.getEntity2().getId().equals("3")) ||
            (s.getEntity1().getId().equals("3") && s.getEntity2().getId().equals("1"))
        );
        assertFalse(foundWrongMatch, "Should not match unrelated entities");
    }
} 