package com.mdm.globalrules.api;

import com.mdm.globalrules.domain.model.GlobalMergeRule;
import com.mdm.globalrules.service.RuleManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for managing global merge rules.
 * Provides endpoints for CRUD operations on global rules.
 */
@RestController
@RequestMapping("/api/global-rules")
public class GlobalRuleController {

    private final RuleManagementService ruleManagementService;

    @Autowired
    public GlobalRuleController(RuleManagementService ruleManagementService) {
        this.ruleManagementService = ruleManagementService;
    }

    /**
     * Creates a new global merge rule.
     * Example DRL for `ruleLogic`:
     * <pre>{@code
     * package com.mdm.rules
     *
     * import com.mdm.botcore.domain.model.MDMEntity; // Assuming this class exists in bot-core
     *
     * rule "ExactCompanyNameMatch"
     * salience 100 // Higher salience means higher priority in Drools
     * when
     * $entity1 : MDMEntity(type == "Organization", name != null)
     * $entity2 : MDMEntity(type == "Organization", name != null, this != $entity1, name == $entity1.name)
     * then
     * // Actions will be handled by the bot, e.g., create a MergeCandidatePair
     * System.out.println("Rule 'ExactCompanyNameMatch' fired for " + $entity1.getName() + " and " + $entity2.getName());
     * end
     * }</pre>
     * @param rule The GlobalMergeRule object to create.
     * @return ResponseEntity with the created rule and HTTP status 201.
     */
    @PostMapping
    public ResponseEntity<GlobalMergeRule> createGlobalRule(@RequestBody GlobalMergeRule rule) {
        GlobalMergeRule createdRule = ruleManagementService.createGlobalRule(rule);
        return new ResponseEntity<>(createdRule, HttpStatus.CREATED);
    }

    /**
     * Retrieves a global merge rule by its ID.
     * @param id The ID of the rule.
     * @return ResponseEntity with the rule if found, or 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GlobalMergeRule> getGlobalRuleById(@PathVariable Long id) {
        return ruleManagementService.getGlobalRuleById(id)
                .map(rule -> new ResponseEntity<>(rule, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves all active global merge rules for a specific domain.
     * @param domain The domain (e.g., "lifescience").
     * @return ResponseEntity with a list of global rules.
     */
    @GetMapping("/domain/{domain}")
    public ResponseEntity<List<GlobalMergeRule>> getGlobalRulesByDomain(@PathVariable String domain) {
        List<GlobalMergeRule> rules = ruleManagementService.getActiveGlobalRulesByDomain(domain);
        return new ResponseEntity<>(rules, HttpStatus.OK);
    }

    /**
     * Updates an existing global merge rule.
     * @param id The ID of the rule to update.
     * @param rule The updated GlobalMergeRule object.
     * @return ResponseEntity with the updated rule, or 404 Not Found.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GlobalMergeRule> updateGlobalRule(@PathVariable Long id, @RequestBody GlobalMergeRule rule) {
        return ruleManagementService.updateGlobalRule(id, rule)
                .map(updatedRule -> new ResponseEntity<>(updatedRule, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Deletes a global merge rule by its ID.
     * @param id The ID of the rule to delete.
     * @return ResponseEntity with 204 No Content if successful, or 404 Not Found.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGlobalRule(@PathVariable Long id) {
        if (ruleManagementService.getGlobalRuleById(id).isPresent()) {
            ruleManagementService.deleteGlobalRule(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}