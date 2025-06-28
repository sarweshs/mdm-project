package com.mdm.botcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdm.botcore.domain.model.MDMEntity;
import org.jeasy.rules.api.Facts;
import org.jeasy.rules.api.Rule;
import org.jeasy.rules.api.Rules;
import org.jeasy.rules.api.RulesEngine;
import org.jeasy.rules.core.DefaultRulesEngine;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("easyRulesRuleEngine")
@Scope("prototype")
public class EasyRulesRuleEngine implements RuleEngine {
    private final ObjectMapper objectMapper;

    public EasyRulesRuleEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<MergeService.MergeSuggestion> processEntities(List<MDMEntity> entities, List<String> rules) {
        List<MergeService.MergeSuggestion> mergeSuggestions = new ArrayList<>();
        if (entities == null || entities.isEmpty()) {
            return mergeSuggestions;
        }
        try {
            Rules easyRules = new Rules();
            easyRules.register(new CompanyNameMatchRule(entities, mergeSuggestions, objectMapper));
            easyRules.register(new AddressMatchRule(entities, mergeSuggestions, objectMapper));
            easyRules.register(new PhoneNumberMatchRule(entities, mergeSuggestions, objectMapper));
            easyRules.register(new EmailDomainMatchRule(entities, mergeSuggestions, objectMapper));
            RulesEngine rulesEngine = new DefaultRulesEngine();
            Facts facts = new Facts();
            rulesEngine.fire(easyRules, facts);
        } catch (Exception e) {
            System.err.println("Error processing entities with Easy Rules: " + e.getMessage());
            e.printStackTrace();
        }
        return mergeSuggestions;
    }

    static class CompanyNameMatchRule implements Rule {
        private final List<MDMEntity> entities;
        private final List<MergeService.MergeSuggestion> mergeSuggestions;
        private final ObjectMapper objectMapper;
        public CompanyNameMatchRule(List<MDMEntity> entities, List<MergeService.MergeSuggestion> mergeSuggestions, ObjectMapper objectMapper) {
            this.entities = entities;
            this.mergeSuggestions = mergeSuggestions;
            this.objectMapper = objectMapper;
        }
        @Override public int getPriority() { return 1; }
        @Override public String getName() { return "ExactCompanyNameMatch"; }
        @Override public boolean evaluate(Facts facts) { return true; }
        @Override public void execute(Facts facts) throws Exception {
            List<MDMEntity> organizations = entities.stream().filter(e -> "Organization".equals(e.getType()) && e.getName() != null).collect(Collectors.toList());
            for (int i = 0; i < organizations.size(); i++) {
                for (int j = i + 1; j < organizations.size(); j++) {
                    MDMEntity entity1 = organizations.get(i);
                    MDMEntity entity2 = organizations.get(j);
                    if (entity1.getName().equalsIgnoreCase(entity2.getName())) {
                        String reasoning = "Company names match exactly: " + entity1.getName();
                        String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                        MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(entity1, entity2, "ExactCompanyNameMatch", reasoning, mergedJson);
                        mergeSuggestions.add(suggestion);
                        System.out.println("EasyRules: Found exact company name match between " + entity1.getName() + " and " + entity2.getName());
                    }
                }
            }
        }
        private MDMEntity createMergedEntity(MDMEntity entity1, MDMEntity entity2) {
            MDMEntity merged = new MDMEntity();
            merged.setId(entity1.getId() + "-" + entity2.getId());
            merged.setType(entity1.getType() != null ? entity1.getType() : entity2.getType());
            merged.setName(entity1.getName() != null ? entity1.getName() : entity2.getName());
            merged.setAddress(entity1.getAddress() != null ? entity1.getAddress() : entity2.getAddress());
            merged.setEmail(entity1.getEmail() != null ? entity1.getEmail() : entity2.getEmail());
            merged.setPhone(entity1.getPhone() != null ? entity1.getPhone() : entity2.getPhone());
            merged.setSourceSystem(entity1.getSourceSystem() != null ? entity1.getSourceSystem() : entity2.getSourceSystem());
            if (entity1.getAttributes() != null || entity2.getAttributes() != null) {
                merged.setAttributes(entity1.getAttributes() != null ? entity1.getAttributes() : entity2.getAttributes());
            }
            return merged;
        }
        @Override public int compareTo(Rule other) { return Integer.compare(this.getPriority(), other.getPriority()); }
    }

    static class AddressMatchRule implements Rule {
        private final List<MDMEntity> entities;
        private final List<MergeService.MergeSuggestion> mergeSuggestions;
        private final ObjectMapper objectMapper;
        public AddressMatchRule(List<MDMEntity> entities, List<MergeService.MergeSuggestion> mergeSuggestions, ObjectMapper objectMapper) {
            this.entities = entities;
            this.mergeSuggestions = mergeSuggestions;
            this.objectMapper = objectMapper;
        }
        @Override public int getPriority() { return 2; }
        @Override public String getName() { return "AddressMatch"; }
        @Override public boolean evaluate(Facts facts) { return true; }
        @Override public void execute(Facts facts) throws Exception {
            List<MDMEntity> withAddress = entities.stream().filter(e -> e.getAddress() != null && e.getAddress().length() > 10).collect(Collectors.toList());
            for (int i = 0; i < withAddress.size(); i++) {
                for (int j = i + 1; j < withAddress.size(); j++) {
                    MDMEntity entity1 = withAddress.get(i);
                    MDMEntity entity2 = withAddress.get(j);
                    if (entity1.getAddress().equalsIgnoreCase(entity2.getAddress())) {
                        String reasoning = "Addresses match: " + entity1.getAddress();
                        String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                        MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(entity1, entity2, "AddressMatch", reasoning, mergedJson);
                        mergeSuggestions.add(suggestion);
                        System.out.println("EasyRules: Found address match between " + entity1.getAddress() + " and " + entity2.getAddress());
                    }
                }
            }
        }
        private MDMEntity createMergedEntity(MDMEntity entity1, MDMEntity entity2) {
            MDMEntity merged = new MDMEntity();
            merged.setId(entity1.getId() + "-" + entity2.getId());
            merged.setType(entity1.getType() != null ? entity1.getType() : entity2.getType());
            merged.setName(entity1.getName() != null ? entity1.getName() : entity2.getName());
            merged.setAddress(entity1.getAddress() != null ? entity1.getAddress() : entity2.getAddress());
            merged.setEmail(entity1.getEmail() != null ? entity1.getEmail() : entity2.getEmail());
            merged.setPhone(entity1.getPhone() != null ? entity1.getPhone() : entity2.getPhone());
            merged.setSourceSystem(entity1.getSourceSystem() != null ? entity1.getSourceSystem() : entity2.getSourceSystem());
            if (entity1.getAttributes() != null || entity2.getAttributes() != null) {
                merged.setAttributes(entity1.getAttributes() != null ? entity1.getAttributes() : entity2.getAttributes());
            }
            return merged;
        }
        @Override public int compareTo(Rule other) { return Integer.compare(this.getPriority(), other.getPriority()); }
    }

    static class PhoneNumberMatchRule implements Rule {
        private final List<MDMEntity> entities;
        private final List<MergeService.MergeSuggestion> mergeSuggestions;
        private final ObjectMapper objectMapper;
        public PhoneNumberMatchRule(List<MDMEntity> entities, List<MergeService.MergeSuggestion> mergeSuggestions, ObjectMapper objectMapper) {
            this.entities = entities;
            this.mergeSuggestions = mergeSuggestions;
            this.objectMapper = objectMapper;
        }
        @Override public int getPriority() { return 3; }
        @Override public String getName() { return "PhoneNumberMatch"; }
        @Override public boolean evaluate(Facts facts) { return true; }
        @Override public void execute(Facts facts) throws Exception {
            List<MDMEntity> withPhone = entities.stream().filter(e -> e.getPhone() != null && e.getPhone().length() >= 10).collect(Collectors.toList());
            for (int i = 0; i < withPhone.size(); i++) {
                for (int j = i + 1; j < withPhone.size(); j++) {
                    MDMEntity entity1 = withPhone.get(i);
                    MDMEntity entity2 = withPhone.get(j);
                    String phone1 = entity1.getPhone().replaceAll("[^0-9]", "");
                    String phone2 = entity2.getPhone().replaceAll("[^0-9]", "");
                    if (phone1.equals(phone2)) {
                        String reasoning = "Phone numbers match: " + entity1.getPhone() + " = " + entity2.getPhone();
                        String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                        MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(entity1, entity2, "PhoneNumberMatch", reasoning, mergedJson);
                        mergeSuggestions.add(suggestion);
                        System.out.println("EasyRules: Found phone number match between " + entity1.getPhone() + " and " + entity2.getPhone());
                    }
                }
            }
        }
        private MDMEntity createMergedEntity(MDMEntity entity1, MDMEntity entity2) {
            MDMEntity merged = new MDMEntity();
            merged.setId(entity1.getId() + "-" + entity2.getId());
            merged.setType(entity1.getType() != null ? entity1.getType() : entity2.getType());
            merged.setName(entity1.getName() != null ? entity1.getName() : entity2.getName());
            merged.setAddress(entity1.getAddress() != null ? entity1.getAddress() : entity2.getAddress());
            merged.setEmail(entity1.getEmail() != null ? entity1.getEmail() : entity2.getEmail());
            merged.setPhone(entity1.getPhone() != null ? entity1.getPhone() : entity2.getPhone());
            merged.setSourceSystem(entity1.getSourceSystem() != null ? entity1.getSourceSystem() : entity2.getSourceSystem());
            if (entity1.getAttributes() != null || entity2.getAttributes() != null) {
                merged.setAttributes(entity1.getAttributes() != null ? entity1.getAttributes() : entity2.getAttributes());
            }
            return merged;
        }
        @Override public int compareTo(Rule other) { return Integer.compare(this.getPriority(), other.getPriority()); }
    }

    static class EmailDomainMatchRule implements Rule {
        private final List<MDMEntity> entities;
        private final List<MergeService.MergeSuggestion> mergeSuggestions;
        private final ObjectMapper objectMapper;
        public EmailDomainMatchRule(List<MDMEntity> entities, List<MergeService.MergeSuggestion> mergeSuggestions, ObjectMapper objectMapper) {
            this.entities = entities;
            this.mergeSuggestions = mergeSuggestions;
            this.objectMapper = objectMapper;
        }
        @Override public int getPriority() { return 4; }
        @Override public String getName() { return "EmailDomainMatch"; }
        @Override public boolean evaluate(Facts facts) { return true; }
        @Override public void execute(Facts facts) throws Exception {
            List<MDMEntity> withEmail = entities.stream().filter(e -> e.getEmail() != null && e.getEmail().contains("@")).collect(Collectors.toList());
            for (int i = 0; i < withEmail.size(); i++) {
                for (int j = i + 1; j < withEmail.size(); j++) {
                    MDMEntity entity1 = withEmail.get(i);
                    MDMEntity entity2 = withEmail.get(j);
                    String domain1 = entity1.getEmail().substring(entity1.getEmail().indexOf("@"));
                    String domain2 = entity2.getEmail().substring(entity2.getEmail().indexOf("@"));
                    if (domain1.equals(domain2)) {
                        String reasoning = "Email domains match: " + domain1;
                        String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                        MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(entity1, entity2, "EmailDomainMatch", reasoning, mergedJson);
                        mergeSuggestions.add(suggestion);
                        System.out.println("EasyRules: Found email domain match between " + domain1 + " and " + domain2);
                    }
                }
            }
        }
        private MDMEntity createMergedEntity(MDMEntity entity1, MDMEntity entity2) {
            MDMEntity merged = new MDMEntity();
            merged.setId(entity1.getId() + "-" + entity2.getId());
            merged.setType(entity1.getType() != null ? entity1.getType() : entity2.getType());
            merged.setName(entity1.getName() != null ? entity1.getName() : entity2.getName());
            merged.setAddress(entity1.getAddress() != null ? entity1.getAddress() : entity2.getAddress());
            merged.setEmail(entity1.getEmail() != null ? entity1.getEmail() : entity2.getEmail());
            merged.setPhone(entity1.getPhone() != null ? entity1.getPhone() : entity2.getPhone());
            merged.setSourceSystem(entity1.getSourceSystem() != null ? entity1.getSourceSystem() : entity2.getSourceSystem());
            if (entity1.getAttributes() != null || entity2.getAttributes() != null) {
                merged.setAttributes(entity1.getAttributes() != null ? entity1.getAttributes() : entity2.getAttributes());
            }
            return merged;
        }
        @Override public int compareTo(Rule other) { return Integer.compare(this.getPriority(), other.getPriority()); }
    }
} 