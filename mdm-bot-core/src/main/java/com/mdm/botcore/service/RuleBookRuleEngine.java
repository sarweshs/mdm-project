package com.mdm.botcore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdm.botcore.domain.model.MDMEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("ruleBookRuleEngine")
@Scope("prototype")
public class RuleBookRuleEngine implements RuleEngine {
    
    private final ObjectMapper objectMapper;
    
    public RuleBookRuleEngine(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<MergeService.MergeSuggestion> processEntities(List<MDMEntity> entities, List<String> rules) {
        List<MergeService.MergeSuggestion> mergeSuggestions = new ArrayList<>();
        
        if (entities == null || entities.isEmpty()) {
            return mergeSuggestions;
        }

        try {
            // Execute all rule types
            mergeSuggestions.addAll(executeExactCompanyNameMatch(entities));
            mergeSuggestions.addAll(executePhoneNumberMatch(entities));
            mergeSuggestions.addAll(executeAddressMatch(entities));
            mergeSuggestions.addAll(executeEmailDomainMatch(entities));
            
        } catch (Exception e) {
            System.err.println("Error processing entities with RuleBook: " + e.getMessage());
            e.printStackTrace();
        }
        
        return mergeSuggestions;
    }
    
    private List<MergeService.MergeSuggestion> executeExactCompanyNameMatch(List<MDMEntity> entities) {
        List<MergeService.MergeSuggestion> suggestions = new ArrayList<>();
        
        List<MDMEntity> organizations = entities.stream()
            .filter(e -> "Organization".equals(e.getType()) && e.getName() != null)
            .collect(Collectors.toList());
            
        for (int i = 0; i < organizations.size(); i++) {
            for (int j = i + 1; j < organizations.size(); j++) {
                MDMEntity entity1 = organizations.get(i);
                MDMEntity entity2 = organizations.get(j);
                
                if (entity1.getName().equalsIgnoreCase(entity2.getName())) {
                    try {
                        String reasoning = "Company names match exactly: " + entity1.getName();
                        String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                        
                        MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(
                            entity1, entity2, "ExactCompanyNameMatch", reasoning, mergedJson
                        );
                        suggestions.add(suggestion);
                        System.out.println("RuleBook: Found exact company name match between " + entity1.getName() + " and " + entity2.getName());
                    } catch (Exception e) {
                        System.err.println("Error creating merge suggestion: " + e.getMessage());
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    private List<MergeService.MergeSuggestion> executePhoneNumberMatch(List<MDMEntity> entities) {
        List<MergeService.MergeSuggestion> suggestions = new ArrayList<>();
        
        List<MDMEntity> entitiesWithPhone = entities.stream()
            .filter(e -> e.getPhone() != null && e.getPhone().length() >= 10)
            .collect(Collectors.toList());
            
        for (int i = 0; i < entitiesWithPhone.size(); i++) {
            for (int j = i + 1; j < entitiesWithPhone.size(); j++) {
                MDMEntity entity1 = entitiesWithPhone.get(i);
                MDMEntity entity2 = entitiesWithPhone.get(j);
                
                String phone1 = entity1.getPhone().replaceAll("[^0-9]", "");
                String phone2 = entity2.getPhone().replaceAll("[^0-9]", "");
                
                if (phone1.equals(phone2)) {
                    try {
                        String reasoning = "Phone numbers match: " + entity1.getPhone() + " = " + entity2.getPhone();
                        String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                        
                        MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(
                            entity1, entity2, "PhoneNumberMatch", reasoning, mergedJson
                        );
                        suggestions.add(suggestion);
                        System.out.println("RuleBook: Found phone number match between " + entity1.getPhone() + " and " + entity2.getPhone());
                    } catch (Exception e) {
                        System.err.println("Error creating merge suggestion: " + e.getMessage());
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    private List<MergeService.MergeSuggestion> executeAddressMatch(List<MDMEntity> entities) {
        List<MergeService.MergeSuggestion> suggestions = new ArrayList<>();
        
        List<MDMEntity> entitiesWithAddress = entities.stream()
            .filter(e -> e.getAddress() != null && e.getAddress().length() > 10)
            .collect(Collectors.toList());
            
        for (int i = 0; i < entitiesWithAddress.size(); i++) {
            for (int j = i + 1; j < entitiesWithAddress.size(); j++) {
                MDMEntity entity1 = entitiesWithAddress.get(i);
                MDMEntity entity2 = entitiesWithAddress.get(j);
                
                if (entity1.getAddress().equalsIgnoreCase(entity2.getAddress())) {
                    try {
                        String reasoning = "Addresses match: " + entity1.getAddress();
                        String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                        
                        MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(
                            entity1, entity2, "AddressMatch", reasoning, mergedJson
                        );
                        suggestions.add(suggestion);
                        System.out.println("RuleBook: Found address match between " + entity1.getAddress() + " and " + entity2.getAddress());
                    } catch (Exception e) {
                        System.err.println("Error creating merge suggestion: " + e.getMessage());
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    private List<MergeService.MergeSuggestion> executeEmailDomainMatch(List<MDMEntity> entities) {
        List<MergeService.MergeSuggestion> suggestions = new ArrayList<>();
        
        List<MDMEntity> entitiesWithEmail = entities.stream()
            .filter(e -> e.getEmail() != null && e.getEmail().contains("@"))
            .collect(Collectors.toList());
            
        for (int i = 0; i < entitiesWithEmail.size(); i++) {
            for (int j = i + 1; j < entitiesWithEmail.size(); j++) {
                MDMEntity entity1 = entitiesWithEmail.get(i);
                MDMEntity entity2 = entitiesWithEmail.get(j);
                
                String domain1 = entity1.getEmail().substring(entity1.getEmail().indexOf("@"));
                String domain2 = entity2.getEmail().substring(entity2.getEmail().indexOf("@"));
                
                if (domain1.equals(domain2)) {
                    try {
                        String reasoning = "Email domains match: " + domain1;
                        String mergedJson = objectMapper.writeValueAsString(createMergedEntity(entity1, entity2));
                        
                        MergeService.MergeSuggestion suggestion = new MergeService.MergeSuggestion(
                            entity1, entity2, "EmailDomainMatch", reasoning, mergedJson
                        );
                        suggestions.add(suggestion);
                        System.out.println("RuleBook: Found email domain match between " + domain1 + " and " + domain2);
                    } catch (Exception e) {
                        System.err.println("Error creating merge suggestion: " + e.getMessage());
                    }
                }
            }
        }
        
        return suggestions;
    }
    
    private MDMEntity createMergedEntity(MDMEntity entity1, MDMEntity entity2) {
        // Simple merge strategy: prefer non-null values, entity1 takes precedence
        MDMEntity merged = new MDMEntity();
        merged.setId(entity1.getId() + "-" + entity2.getId());
        merged.setType(entity1.getType() != null ? entity1.getType() : entity2.getType());
        merged.setName(entity1.getName() != null ? entity1.getName() : entity2.getName());
        merged.setAddress(entity1.getAddress() != null ? entity1.getAddress() : entity2.getAddress());
        merged.setEmail(entity1.getEmail() != null ? entity1.getEmail() : entity2.getEmail());
        merged.setPhone(entity1.getPhone() != null ? entity1.getPhone() : entity2.getPhone());
        merged.setSourceSystem(entity1.getSourceSystem() != null ? entity1.getSourceSystem() : entity2.getSourceSystem());
        
        // Merge attributes
        if (entity1.getAttributes() != null || entity2.getAttributes() != null) {
            // Simple merge: entity1 attributes take precedence
            merged.setAttributes(entity1.getAttributes() != null ? entity1.getAttributes() : entity2.getAttributes());
        }
        
        return merged;
    }
} 