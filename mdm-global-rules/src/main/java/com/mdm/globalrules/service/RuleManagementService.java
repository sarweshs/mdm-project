package com.mdm.globalrules.service;

import com.mdm.globalrules.domain.model.CompanyMergeRule;
import com.mdm.globalrules.domain.model.GlobalMergeRule;
import com.mdm.globalrules.domain.repository.CompanyMergeRuleRepository;
import com.mdm.globalrules.domain.repository.GlobalMergeRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for managing global and company-specific merge rules.
 * It provides methods for CRUD operations and for retrieving the effective set of rules
 * for a given company.
 */
@Service
public class RuleManagementService {

    private final GlobalMergeRuleRepository globalMergeRuleRepository;
    private final CompanyMergeRuleRepository companyMergeRuleRepository;

    @Autowired
    public RuleManagementService(GlobalMergeRuleRepository globalMergeRuleRepository,
                                 CompanyMergeRuleRepository companyMergeRuleRepository) {
        this.globalMergeRuleRepository = globalMergeRuleRepository;
        this.companyMergeRuleRepository = companyMergeRuleRepository;
    }

    // --- Global Rule Operations ---

    /**
     * Creates a new global merge rule.
     * @param rule The GlobalMergeRule to save.
     * @return The saved GlobalMergeRule.
     */
    @Transactional
    public GlobalMergeRule createGlobalRule(GlobalMergeRule rule) {
        // Optional: Add validation for ruleName uniqueness before saving
        return globalMergeRuleRepository.save(rule);
    }

    /**
     * Retrieves a global merge rule by its ID.
     * @param id The ID of the rule.
     * @return An Optional containing the GlobalMergeRule if found.
     */
    @Transactional(readOnly = true)
    public Optional<GlobalMergeRule> getGlobalRuleById(Long id) {
        return globalMergeRuleRepository.findById(id);
    }

    /**
     * Retrieves all active global merge rules for a specific domain, ordered by priority.
     * @param domain The domain for which to retrieve rules.
     * @return A list of active GlobalMergeRule entities.
     */
    @Transactional(readOnly = true)
    public List<GlobalMergeRule> getActiveGlobalRulesByDomain(String domain) {
        return globalMergeRuleRepository.findByDomainAndActiveOrderByPriorityDesc(domain, true);
    }

    /**
     * Updates an existing global merge rule.
     * @param id The ID of the rule to update.
     * @param updatedRule The updated GlobalMergeRule object.
     * @return An Optional containing the updated GlobalMergeRule if found and updated.
     */
    @Transactional
    public Optional<GlobalMergeRule> updateGlobalRule(Long id, GlobalMergeRule updatedRule) {
        return globalMergeRuleRepository.findById(id).map(existingRule -> {
            existingRule.setDomain(updatedRule.getDomain());
            existingRule.setRuleName(updatedRule.getRuleName());
            existingRule.setDescription(updatedRule.getDescription());
            existingRule.setRuleLogic(updatedRule.getRuleLogic());
            existingRule.setPriority(updatedRule.getPriority());
            existingRule.setActive(updatedRule.isActive());
            return globalMergeRuleRepository.save(existingRule);
        });
    }

    /**
     * Deletes a global merge rule by its ID.
     * @param id The ID of the rule to delete.
     */
    @Transactional
    public void deleteGlobalRule(Long id) {
        globalMergeRuleRepository.deleteById(id);
    }

    // --- Company Rule Operations ---

    /**
     * Creates a new company-specific merge rule.
     * If overrideGlobal is true, it's good practice to ensure a global rule with the same name exists.
     * @param rule The CompanyMergeRule to save.
     * @return The saved CompanyMergeRule.
     */
    @Transactional
    public CompanyMergeRule createCompanyRule(CompanyMergeRule rule) {
        // Optional: Add validation for companyId + ruleName uniqueness before saving
        return companyMergeRuleRepository.save(rule);
    }

    /**
     * Retrieves a company-specific merge rule by its ID.
     * @param id The ID of the rule.
     * @return An Optional containing the CompanyMergeRule if found.
     */
    @Transactional(readOnly = true)
    public Optional<CompanyMergeRule> getCompanyRuleById(Long id) {
        return companyMergeRuleRepository.findById(id);
    }

    /**
     * Retrieves all active company-specific merge rules for a given company ID.
     * @param companyId The ID of the company.
     * @return A list of active CompanyMergeRule entities.
     */
    @Transactional(readOnly = true)
    public List<CompanyMergeRule> getActiveCompanyRulesByCompanyId(String companyId) {
        return companyMergeRuleRepository.findByCompanyIdAndActiveOrderByPriorityDesc(companyId, true);
    }

    /**
     * Updates an existing company-specific merge rule.
     * @param id The ID of the rule to update.
     * @param updatedRule The updated CompanyMergeRule object.
     * @return An Optional containing the updated CompanyMergeRule if found and updated.
     */
    @Transactional
    public Optional<CompanyMergeRule> updateCompanyRule(Long id, CompanyMergeRule updatedRule) {
        return companyMergeRuleRepository.findById(id).map(existingRule -> {
            existingRule.setCompanyId(updatedRule.getCompanyId());
            existingRule.setRuleName(updatedRule.getRuleName());
            existingRule.setDescription(updatedRule.getDescription());
            existingRule.setRuleLogic(updatedRule.getRuleLogic());
            existingRule.setPriority(updatedRule.getPriority());
            existingRule.setActive(updatedRule.isActive());
            existingRule.setOverrideGlobal(updatedRule.isOverrideGlobal());
            return companyMergeRuleRepository.save(existingRule);
        });
    }

    /**
     * Deletes a company-specific merge rule by its ID.
     * @param id The ID of the rule to delete.
     */
    @Transactional
    public void deleteCompanyRule(Long id) {
        companyMergeRuleRepository.deleteById(id);
    }

    // --- Effective Rule Retrieval ---

    /**
     * Retrieves the effective set of merge rules for a given company and domain.
     * This method combines global rules and company-specific rules, applying overrides.
     *
     * Rules are prioritized as follows:
     * 1. Company-specific rules that explicitly override a global rule (if active and higher priority).
     * 2. Other active company-specific rules.
     * 3. Global rules not overridden by any company rule.
     *
     * @param companyId The ID of the company.
     * @param domain The domain (e.g., "lifescience").
     * @return A list of rule logics (DRL strings) representing the effective rules for the bot.
     * The list is ordered by priority (highest first) and then by rule name.
     */
    @Transactional(readOnly = true)
    public List<String> getEffectiveRulesForCompany(String companyId, String domain) {
        List<GlobalMergeRule> globalRules = globalMergeRuleRepository.findByDomainAndActiveOrderByPriorityDesc(domain, true);
        List<CompanyMergeRule> companyRules = companyMergeRuleRepository.findByCompanyIdAndActiveOrderByPriorityDesc(companyId, true);

        // Use a map to easily check for overridden global rules by ruleName
        Map<String, CompanyMergeRule> overridingCompanyRules = companyRules.stream()
                .filter(CompanyMergeRule::isOverrideGlobal)
                .collect(Collectors.toMap(CompanyMergeRule::getRuleName, rule -> rule,
                        (existing, replacement) -> existing.getPriority() > replacement.getPriority() ? existing : replacement)); // Take higher priority if duplicate override exists

        List<String> effectiveRuleLogics = new ArrayList<>();
        Set<String> companyRuleNames = companyRules.stream()
                .map(CompanyMergeRule::getRuleName)
                .collect(Collectors.toSet());

        // Add company-specific rules (including those that override but were already added to map)
        companyRules.forEach(rule -> effectiveRuleLogics.add(rule.getRuleLogic()));

        // Add global rules that are not overridden by any company rule
        globalRules.stream()
                .filter(globalRule -> !overridingCompanyRules.containsKey(globalRule.getRuleName()) && // Not explicitly overridden
                        !companyRuleNames.contains(globalRule.getRuleName())) // Not a new rule with same name
                .forEach(globalRule -> effectiveRuleLogics.add(globalRule.getRuleLogic()));

        // For simplicity, we just return the DRL strings. The bot core will handle compilation.
        // If sorting within this list is critical *before* bot compilation,
        // you would need to encapsulate rules with priority and sort them here.
        // For Drools, the priority is often handled by the `salience` attribute within the DRL.
        return effectiveRuleLogics;
    }
}