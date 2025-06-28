package com.mdm.reviewdashboard.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class MergeCandidatePair {
    private Long id;
    
    @JsonProperty("entity1Json")
    private String entity1Json;
    
    @JsonProperty("entity2Json")
    private String entity2Json;
    
    private String companyId;
    private String domain;
    private MergeStatus status;
    
    @JsonProperty("reasoningJson")
    private String reasoningJson;
    
    private Double confidenceScore;
    
    @JsonProperty("proposedMergedEntityJson")
    private String proposedMergedEntityJson;
    
    @JsonProperty("reviewComment")
    private String reviewComment;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;

    public enum MergeStatus {
        PENDING_REVIEW,
        APPROVED,
        REJECTED,
        MERGED
    }

    // Constructors
    public MergeCandidatePair() {}

    public MergeCandidatePair(String entity1Json, String entity2Json, String companyId, String domain) {
        this.entity1Json = entity1Json;
        this.entity2Json = entity2Json;
        this.companyId = companyId;
        this.domain = domain;
        this.status = MergeStatus.PENDING_REVIEW;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntity1Json() {
        return entity1Json;
    }

    public void setEntity1Json(String entity1Json) {
        this.entity1Json = entity1Json;
    }

    public String getEntity2Json() {
        return entity2Json;
    }

    public void setEntity2Json(String entity2Json) {
        this.entity2Json = entity2Json;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public MergeStatus getStatus() {
        return status;
    }

    public void setStatus(MergeStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getReasoningJson() {
        return reasoningJson;
    }

    public void setReasoningJson(String reasoningJson) {
        this.reasoningJson = reasoningJson;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getProposedMergedEntityJson() {
        return proposedMergedEntityJson;
    }

    public void setProposedMergedEntityJson(String proposedMergedEntityJson) {
        this.proposedMergedEntityJson = proposedMergedEntityJson;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "MergeCandidatePair{" +
                "id=" + id +
                ", entity1Json='" + entity1Json + '\'' +
                ", entity2Json='" + entity2Json + '\'' +
                ", companyId='" + companyId + '\'' +
                ", domain='" + domain + '\'' +
                ", status=" + status +
                ", reasoningJson='" + reasoningJson + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", proposedMergedEntityJson='" + proposedMergedEntityJson + '\'' +
                ", reviewComment='" + reviewComment + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
} 