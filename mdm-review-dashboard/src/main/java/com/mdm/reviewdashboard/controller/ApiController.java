package com.mdm.reviewdashboard.controller;

import com.mdm.reviewdashboard.domain.MergeCandidatePair;
import com.mdm.reviewdashboard.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST API Controller for the review dashboard.
 * Provides endpoints for the bot-nlp-dashboard and other API consumers.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

    private final ReviewService reviewService;

    @Autowired
    public ApiController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Retrieves all merge candidate pairs that are pending review.
     * @return A list of MergeCandidatePair objects.
     */
    @GetMapping("/merge/candidates/pending-review")
    public ResponseEntity<List<MergeCandidatePair>> getPendingMergeCandidates() {
        List<MergeCandidatePair> pendingCandidates = reviewService.getPendingMergeCandidates();
        return ResponseEntity.ok(pendingCandidates);
    }

    /**
     * Retrieves all merge candidate pairs that have been approved.
     * @return A list of MergeCandidatePair objects.
     */
    @GetMapping("/merge/candidates/approved")
    public ResponseEntity<List<MergeCandidatePair>> getApprovedMergeCandidates() {
        List<MergeCandidatePair> approvedCandidates = reviewService.getApprovedMergeCandidates();
        return ResponseEntity.ok(approvedCandidates);
    }

    /**
     * Retrieves all merge candidate pairs that have been rejected.
     * @return A list of MergeCandidatePair objects.
     */
    @GetMapping("/merge/candidates/rejected")
    public ResponseEntity<List<MergeCandidatePair>> getRejectedMergeCandidates() {
        List<MergeCandidatePair> rejectedCandidates = reviewService.getRejectedMergeCandidates();
        return ResponseEntity.ok(rejectedCandidates);
    }

    /**
     * Retrieves all merge candidate pairs regardless of status.
     * @return A list of MergeCandidatePair objects.
     */
    @GetMapping("/merge/candidates/all")
    public ResponseEntity<List<MergeCandidatePair>> getAllMergeCandidates() {
        List<MergeCandidatePair> allCandidates = reviewService.getAllMergeCandidates();
        return ResponseEntity.ok(allCandidates);
    }

    /**
     * Updates the status of a merge candidate.
     * @param id The ID of the merge candidate pair.
     * @param request A map containing "status" and optional "comment".
     * @return The updated MergeCandidatePair.
     */
    @PutMapping("/merge/candidates/{id}/status")
    public ResponseEntity<MergeCandidatePair> updateMergeCandidateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String status = request.get("status");
        String comment = request.get("comment");
        
        Optional<MergeCandidatePair> updatedPair = Optional.empty();
        
        if ("APPROVED".equalsIgnoreCase(status)) {
            updatedPair = reviewService.approveMergeCandidate(id, comment);
        } else if ("REJECTED".equalsIgnoreCase(status)) {
            updatedPair = reviewService.rejectMergeCandidate(id, comment);
        } else {
            return ResponseEntity.badRequest().build();
        }
        
        return updatedPair
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 