package com.mdm.botcore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdm.botcore.config.DroolsConfig;
import com.mdm.botcore.domain.model.AuditLog;
import com.mdm.botcore.domain.model.MDMEntity;
import com.mdm.botcore.domain.model.MergeCandidatePair;
import com.mdm.botcore.domain.repository.AuditLogRepository;
import com.mdm.botcore.domain.repository.MergeCandidatePairRepository;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service responsible for the core entity merging logic using Drools.
 * It fetches rules, executes them, identifies merge candidates, and logs audit trails.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE) // Ensure new instance for each merge process to avoid state issues with KieSession
public class MergeService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final MergeCandidatePairRepository mergeCandidatePairRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationContext applicationContext; // To get prototype beans
    private final RuleEngine ruleEngine;

    @Autowired
    public MergeService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper,
                        MergeCandidatePairRepository mergeCandidatePairRepository,
                        AuditLogRepository auditLogRepository,
                        ApplicationContext applicationContext,
                        @Qualifier("droolsRuleEngine") RuleEngine droolsRuleEngine,
                        @Qualifier("ruleBookRuleEngine") RuleEngine ruleBookRuleEngine,
                        @Qualifier("easyRulesRuleEngine") RuleEngine easyRulesRuleEngine,
                        @Value("${rule.engine:easyrules}") String ruleEngineType,
                        @Value("${mdm.global-rules.base-url}") String globalRulesBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(globalRulesBaseUrl).build();
        this.objectMapper = objectMapper;
        this.mergeCandidatePairRepository = mergeCandidatePairRepository;
        this.auditLogRepository = auditLogRepository;
        this.applicationContext = applicationContext;
        if ("drools".equalsIgnoreCase(ruleEngineType)) {
            this.ruleEngine = droolsRuleEngine;
        } else if ("rulebook".equalsIgnoreCase(ruleEngineType)) {
            this.ruleEngine = ruleBookRuleEngine;
        } else {
            this.ruleEngine = easyRulesRuleEngine;
        }
    }

    /**
     * Fetches effective DRL rules from the mdm-global-rules service.
     * @param companyId The ID of the company for which to get rules.
     * @param domain The domain (e.g., "lifescience").
     * @return A list of DRL rule strings.
     */
    private List<String> fetchEffectiveRules(String companyId, String domain) {
        try {
            // Using WebClient to call the mdm-global-rules service
            String jsonResponse = webClient.get()
                    .uri("/api/company-rules/effective/{companyId}/{domain}", companyId, domain)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); // Block to make it synchronous for now

            if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
                System.out.println("No effective rules found for companyId: " + companyId + ", domain: " + domain);
                return new ArrayList<>();
            }

            // Parse the JSON array response to extract individual DRL strings
            List<String> rules = objectMapper.readValue(jsonResponse, 
                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));

            if (rules == null || rules.isEmpty()) {
                System.out.println("No effective rules found for companyId: " + companyId + ", domain: " + domain);
                return new ArrayList<>();
            }
            
            System.out.println("Fetched " + rules.size() + " effective rules.");
            return rules;
        } catch (Exception e) {
            System.err.println("Error fetching effective rules: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return new ArrayList<>();
        }
    }

    /**
     * Processes a list of entities to identify potential merge candidates using Drools rules.
     *
     * @param entities A list of MDMEntity objects to evaluate for merges.
     * @param companyId The ID of the company for which to apply rules.
     * @param domain The domain (e.g., "lifescience").
     */
    @Transactional
    public void processEntitiesForMerge(List<MDMEntity> entities, String companyId, String domain) {
        if (entities == null || entities.isEmpty()) {
            System.out.println("No entities provided for merge processing.");
            return;
        }

        // 1. Fetch effective rules dynamically
        List<String> rules = fetchEffectiveRules(companyId, domain);
        if (rules.isEmpty()) {
            System.out.println("No rules to apply. Skipping merge processing.");
            return;
        }

        // 2. Use the selected RuleEngine
        List<MergeSuggestion> mergeSuggestions = ruleEngine.processEntities(entities, rules);

        // 3. Process merge suggestions
        if (!mergeSuggestions.isEmpty()) {
            System.out.println("Found " + mergeSuggestions.size() + " merge suggestions.");
            for (MergeSuggestion suggestion : mergeSuggestions) {
                try {
                    MergeCandidatePair candidatePair = new MergeCandidatePair(
                            objectMapper.writeValueAsString(suggestion.getEntity1()),
                            objectMapper.writeValueAsString(suggestion.getEntity2()),
                            suggestion.getProposedMergedEntityJson(),
                            suggestion.getReasoningJson()
                    );
                    mergeCandidatePairRepository.save(candidatePair);
                    System.out.println("Saved merge candidate pair: " + candidatePair.getId());

                    AuditLog auditLog = new AuditLog(
                            candidatePair,
                            suggestion.getRuleName(),
                            suggestion.getReasoningJson(),
                            suggestion.getEntity1().getId(),
                            suggestion.getEntity2().getId(),
                            true
                    );
                    auditLogRepository.save(auditLog);
                    System.out.println("Saved audit log for merge: " + auditLog.getId());
                } catch (JsonProcessingException e) {
                    System.err.println("Error processing JSON for entities: " + e.getMessage());
                }
            }
        } else {
            System.out.println("No merge suggestions generated by the rules for this batch.");
        }
    }

    /**
     * This class acts as a container for facts inserted into the KieSession.
     * Rules will populate this to signal a merge recommendation.
     * DRL rules can create instances of this class and insert them into the KieSession,
     * or modify an existing instance if it's passed as a global.
     *
     * Example of how a DRL rule would use this:
     * rule "ExactCompanyNameMatch"
     * when
     * $entity1 : MDMEntity(type == "Organization", name != null)
     * $entity2 : MDMEntity(type == "Organization", name != null, this != $entity1, name == $entity1.name)
     * then
     * MDMEntity merged = new MDMEntity();
     * // Copy relevant attributes, merge strategy (e.g., latest, union)
     * merged.setId($entity1.getId() + "-" + $entity2.getId()); // Example ID generation
     * merged.setName($entity1.getName());
     * // ... more complex merge logic ...
     * insert(new com.mdm.botcore.service.MergeService.MergeSuggestion($entity1, $entity2, "ExactCompanyNameMatch", "Company names match: " + $entity1.getName(), objectMapper.writeValueAsString(merged)));
     * end
     *
     * Note: The fully qualified class name for MergeSuggestion must be used in DRL rules.
     * Also, the `objectMapper` global allows using `objectMapper.writeValueAsString` in DRL.
     */
    public static class MergeSuggestion {
        private MDMEntity entity1;
        private MDMEntity entity2;
        private String ruleName;
        private String reasoningJson; // Detailed reason for the merge suggestion
        private String proposedMergedEntityJson; // JSON representation of the resulting merged entity

        public MergeSuggestion(MDMEntity entity1, MDMEntity entity2, String ruleName, String reasoningJson, String proposedMergedEntityJson) {
            this.entity1 = entity1;
            this.entity2 = entity2;
            this.ruleName = ruleName;
            this.reasoningJson = reasoningJson;
            this.proposedMergedEntityJson = proposedMergedEntityJson;
        }

        public MDMEntity getEntity1() { return entity1; }
        public MDMEntity getEntity2() { return entity2; }
        public String getRuleName() { return ruleName; }
        public String getReasoningJson() { return reasoningJson; }
        public String getProposedMergedEntityJson() { return proposedMergedEntityJson; }
    }


    /**
     * Updates the status of a MergeCandidatePair based on human review.
     * This is called by the Review Dashboard.
     * @param pairId The ID of the merge candidate pair.
     * @param status The new status (APPROVED or REJECTED).
     * @param comment Optional comment from the reviewer.
     * @return The updated MergeCandidatePair, or empty if not found.
     */
    @Transactional
    public Optional<MergeCandidatePair> updateMergeCandidateStatus(Long pairId, MergeCandidatePair.MergeStatus status, String comment) {
        return mergeCandidatePairRepository.findById(pairId).map(pair -> {
            pair.setStatus(status);
            pair.setReviewComment(comment);
            // In a real scenario, if approved, you'd trigger the actual merge into a "golden record" system here.
            // If rejected, you might use this feedback for bot memory/learning.
            return mergeCandidatePairRepository.save(pair);
        });
    }

    /**
     * Retrieves all merge candidate pairs with a specific status.
     * @param status The status to filter by.
     * @return A list of MergeCandidatePair entities.
     */
    @Transactional(readOnly = true)
    public List<MergeCandidatePair> getMergeCandidatesByStatus(MergeCandidatePair.MergeStatus status) {
        return mergeCandidatePairRepository.findByStatus(status);
    }

    /**
     * Retrieves all merge candidate pairs regardless of status.
     * @return A list of all MergeCandidatePair entities.
     */
    @Transactional(readOnly = true)
    public List<MergeCandidatePair> getAllMergeCandidates() {
        return mergeCandidatePairRepository.findAll();
    }

    /**
     * Retrieves a single merge candidate pair by its ID.
     * @param id The ID of the merge candidate pair.
     * @return An Optional containing the MergeCandidatePair if found.
     */
    @Transactional(readOnly = true)
    public Optional<MergeCandidatePair> getMergeCandidateById(Long id) {
        return mergeCandidatePairRepository.findById(id);
    }

    /**
     * Retrieves audit logs for a specific merge candidate pair ID.
     * @param mergeCandidatePairId The ID of the merge candidate pair.
     * @return A list of matching AuditLog entries.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getAuditLogsForMergeCandidate(Long mergeCandidatePairId) {
        return auditLogRepository.findByMergeCandidatePairId(mergeCandidatePairId);
    }
}