package com.mdm.globalrules.api;

import com.mdm.globalrules.domain.model.CompanyMergeRule;
import com.mdm.globalrules.service.RuleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing company-specific merge rules.
 * Provides endpoints for CRUD operations on company rules.
 */
@RestController
@RequestMapping("/api/company-rules")
public class CompanyRuleController {

    private final RuleManagementService ruleManagementService;

    @Autowired
    public CompanyRuleController(RuleManagementService ruleManagementService) {
        this.ruleManagementService = ruleManagementService;
    }

    /**
     * Creates a new company-specific merge rule.
     * @param rule The CompanyMergeRule object to create.
     * @return ResponseEntity with the created rule and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<CompanyMergeRule> createCompanyRule(@RequestBody CompanyMergeRule rule) {
        CompanyMergeRule createdRule = ruleManagementService.createCompanyRule(rule);
        return new ResponseEntity<>(createdRule, HttpStatus.CREATED);
    }

    /**
     * Retrieves a company-specific merge rule by its ID.
     * @param id The ID of the rule.
     * @return ResponseEntity with the rule if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CompanyMergeRule> getCompanyRuleById(@PathVariable Long id) {
        return ruleManagementService.getCompanyRuleById(id)
                .map(rule -> new ResponseEntity<>(rule, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves all active company-specific merge rules for a given company ID.
     * @param companyId The ID of the company.
     * @return ResponseEntity with a list of company rules.
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<CompanyMergeRule>> getCompanyRulesByCompanyId(@PathVariable String companyId) {
        List<CompanyMergeRule> rules = ruleManagementService.getActiveCompanyRulesByCompanyId(companyId);
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }

    /**
     * Retrieves the effective set of DRL rules for a specific company and domain,
     * taking into account global rules and company-specific overrides.
     * This endpoint is intended to be called by the `mdm-bot-core` service.
     * @param companyId The ID of the company.
     * @param domain The domain (e.g., "lifescience").
     * @return ResponseEntity with a list of DRL strings.
     */
    @GetMapping("/effective/{companyId}/{domain}")
    public ResponseEntity<List<String>> getEffectiveRulesForCompany(
            @PathVariable String companyId,
            @PathVariable String domain) {
        List<String> effectiveRules = ruleManagementService.getEffectiveRulesForCompany(companyId, domain);
        return new ResponseEntity<>(effectiveRules, HttpStatus.OK);
    }

    /**
     * Updates an existing company-specific merge rule.
     * @param id The ID of the rule to update.
     * @param rule The updated CompanyMergeRule object.
     * @return ResponseEntity with the updated rule, or 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CompanyMergeRule> updateCompanyRule(@PathVariable Long id, @RequestBody CompanyMergeRule rule) {
        return ruleManagementService.updateCompanyRule(id, rule)
                .map(updatedRule -> new ResponseEntity<>(updatedRule, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Deletes a company-specific merge rule by its ID.
     * @param id The ID of the rule to delete.
     * @return ResponseEntity with 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompanyRule(@PathVariable Long id) {
        if (ruleManagementService.getCompanyRuleById(id).isPresent()) {
            ruleManagementService.deleteCompanyRule(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}