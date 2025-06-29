package com.mdm.botcore.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdm.botcore.domain.model.MDMEntity;
import com.mdm.botcore.domain.model.AuditLog;
import com.mdm.botcore.domain.model.MergeCandidatePair;
import com.mdm.botcore.service.MergeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for the MDM Bot Core service.
 * Provides endpoints to trigger merge processing and manage merge candidates.
 */
@RestController
@RequestMapping("/api/merge")
public class MergeController {

    private final MergeService mergeService;
    private final ObjectMapper objectMapper; // For converting Map<String, Object> to MDMEntity

    @Autowired
    public MergeController(MergeService mergeService, ObjectMapper objectMapper) {
        this.mergeService = mergeService;
        this.objectMapper = objectMapper;
    }

    /**
     * Endpoint to trigger the bot to process a list of entities for potential merges.
     * @param request A map containing "companyId", "domain", and a list of "entities".
     * Example request body:
     * <pre>{@code
     * {
     * "companyId": "COMPANY_A",
     * "domain": "lifescience",
     * "entities": [
     * {"id": "E1", "type": "Organization", "name": "Acme Corp", "address": "123 Main St", "sourceSystem": "CRM"},
     * {"id": "E2", "type": "Organization", "name": "Acme Corp", "address": "456 Oak Ave", "sourceSystem": "ERP"},
     * {"id": "E3", "type": "Person", "name": "John Doe", "email": "john.doe@example.com", "sourceSystem": "HR"}
     * ]
     * }}</pre>
     * @return ResponseEntity indicating success or failure.
     */
    @PostMapping("/process-entities")
    public ResponseEntity<String> processEntities(@RequestBody Map<String, Object> request) {
        String companyId = (String) request.get("companyId");
        String domain = (String) request.get("domain");
        List<Map<String, Object>> entityMaps = (List<Map<String, Object>>) request.get("entities");

        if (companyId == null || domain == null || entityMaps == null || entityMaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing companyId, domain, or entities in request.");
        }

        // Convert Map<String, Object> to MDMEntity objects
        List<MDMEntity> entities = entityMaps.stream()
                .map(map -> objectMapper.convertValue(map, MDMEntity.class))
                .collect(Collectors.toList());

        mergeService.processEntitiesForMerge(entities, companyId, domain);
        return new ResponseEntity<>("Entities submitted for merge processing.", HttpStatus.OK);
    }

    /**
     * Retrieves all merge candidate pairs that are pending review.
     * This endpoint is intended for the human review dashboard.
     * @return A list of MergeCandidatePair objects.
     */
    @GetMapping("/candidates/pending-review")
    public ResponseEntity<List<MergeCandidatePair>> getPendingMergeCandidates() {
        List<MergeCandidatePair> pendingCandidates = mergeService.getMergeCandidatesByStatus(MergeCandidatePair.MergeStatus.PENDING_REVIEW);
        return new ResponseEntity<>(pendingCandidates, HttpStatus.OK);
    }

    /**
     * Endpoint for human agents to approve or reject a merge candidate.
     * @param id The ID of the MergeCandidatePair.
     * @param statusUpdateRequest A map containing "status" ("APPROVED" or "REJECTED") and optional "comment".
     * @return ResponseEntity with the updated MergeCandidatePair.
     */
    @PutMapping("/candidates/{id}/status")
    public ResponseEntity<MergeCandidatePair> updateMergeCandidateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> statusUpdateRequest) {
        String statusString = statusUpdateRequest.get("status");
        String comment = statusUpdateRequest.get("comment");

        if (statusString == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required.");
        }

        MergeCandidatePair.MergeStatus newStatus;
        try {
            newStatus = MergeCandidatePair.MergeStatus.valueOf(statusString.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status: " + statusString);
        }

        return mergeService.updateMergeCandidateStatus(id, newStatus, comment)
                .map(updatedPair -> new ResponseEntity<>(updatedPair, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Retrieves audit logs for a specific merge candidate pair.
     * This endpoint is intended for the human review dashboard to show historical reasoning.
     * @param id The ID of the merge candidate pair.
     * @return A list of AuditLog objects.
     */
    @GetMapping("/candidates/{id}/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogsForCandidate(@PathVariable Long id) {
        List<AuditLog> auditLogs = mergeService.getAuditLogsForMergeCandidate(id);
        return new ResponseEntity<>(auditLogs, HttpStatus.OK);
    }
}