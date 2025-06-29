package com.mdm.reviewdashboard.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdm.reviewdashboard.domain.MDMEntity;
import com.mdm.reviewdashboard.domain.MergeCandidatePair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service for interacting with the mdm-bot-core service to fetch and update merge candidates.
 */
@Service
public class ReviewService {

    private final WebClient webClient;
    private final String botCoreBaseUrl;
    private final ObjectMapper objectMapper;

    // Inject base URL for the bot-core service from application.properties
    public ReviewService(WebClient webClient, @Value("${mdm.bot.core.base-url}") String botCoreBaseUrl, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.botCoreBaseUrl = botCoreBaseUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches all merge candidate pairs that are pending human review.
     * @return A list of MergeCandidatePair objects.
     */
    public List<MergeCandidatePair> getPendingMergeCandidates() {
        // Use ParameterizedTypeReference to correctly deserialize List<MergeCandidatePair>
        return webClient.get()
                .uri(botCoreBaseUrl + "/api/merge/candidates/pending-review")
                .retrieve()
                .bodyToFlux(MergeCandidatePair.class)
                .collectList()
                .block(); // Block to make it synchronous for simple Thymeleaf rendering
    }

    /**
     * Fetches all merge candidate pairs that have been approved.
     * @return A list of MergeCandidatePair objects.
     */
    public List<MergeCandidatePair> getApprovedMergeCandidates() {
        return webClient.get()
                .uri(botCoreBaseUrl + "/api/merge/candidates/approved")
                .retrieve()
                .bodyToFlux(MergeCandidatePair.class)
                .collectList()
                .block();
    }

    /**
     * Fetches all merge candidate pairs that have been rejected.
     * @return A list of MergeCandidatePair objects.
     */
    public List<MergeCandidatePair> getRejectedMergeCandidates() {
        return webClient.get()
                .uri(botCoreBaseUrl + "/api/merge/candidates/rejected")
                .retrieve()
                .bodyToFlux(MergeCandidatePair.class)
                .collectList()
                .block();
    }

    /**
     * Fetches all merge candidate pairs regardless of status.
     * @return A list of MergeCandidatePair objects.
     */
    public List<MergeCandidatePair> getAllMergeCandidates() {
        return webClient.get()
                .uri(botCoreBaseUrl + "/api/merge/candidates/all")
                .retrieve()
                .bodyToFlux(MergeCandidatePair.class)
                .collectList()
                .block();
    }

    /**
     * Approves a merge candidate.
     * @param id The ID of the merge candidate pair.
     * @param comment Optional comment from the reviewer.
     * @return The updated MergeCandidatePair, or empty if not found.
     */
    public Optional<MergeCandidatePair> approveMergeCandidate(Long id, String comment) {
        Map<String, String> requestBody = Map.of(
                "status", MergeCandidatePair.MergeStatus.APPROVED.name(),
                "comment", comment != null ? comment : "Approved by human agent."
        );
        return webClient.put()
                .uri(botCoreBaseUrl + "/api/merge/candidates/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(MergeCandidatePair.class)
                .blockOptional(); // Block and get Optional result
    }

    /**
     * Rejects a merge candidate.
     * @param id The ID of the merge candidate pair.
     * @param comment Optional comment from the reviewer.
     * @return The updated MergeCandidatePair, or empty if not found.
     */
    public Optional<MergeCandidatePair> rejectMergeCandidate(Long id, String comment) {
        Map<String, String> requestBody = Map.of(
                "status", MergeCandidatePair.MergeStatus.REJECTED.name(),
                "comment", comment != null ? comment : "Rejected by human agent."
        );
        return webClient.put()
                .uri(botCoreBaseUrl + "/api/merge/candidates/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestBody))
                .retrieve()
                .bodyToMono(MergeCandidatePair.class)
                .blockOptional();
    }

    /**
     * Helper method to deserialize JSON string of MDMEntity back into MDMEntity object.
     * @param jsonString The JSON string representation of an MDMEntity.
     * @return An Optional containing the MDMEntity if deserialization is successful.
     */
    public Optional<MDMEntity> deserializeMdmEntity(String jsonString) {
        try {
            return Optional.of(objectMapper.readValue(jsonString, MDMEntity.class));
        } catch (Exception e) {
            System.err.println("Error deserializing MDMEntity from JSON: " + e.getMessage());
            return Optional.empty();
        }
    }
}