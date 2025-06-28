package com.mdm.botcore.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a pair of entities identified by the bot as potential merge candidates.
 * This entity is stored in the database for human review.
 */
@Entity
@Table(name = "merge_candidate_pairs")
@EntityListeners(AuditingEntityListener.class)
public class MergeCandidatePair {

    public enum MergeStatus {
        PENDING_REVIEW, APPROVED, REJECTED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT") // Store JSON of original entity 1
    private String entity1Json;

    @Column(nullable = false, columnDefinition = "TEXT") // Store JSON of original entity 2
    private String entity2Json;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MergeStatus status = MergeStatus.PENDING_REVIEW;

    @Column(columnDefinition = "TEXT") // JSON representation of the proposed merged entity
    private String proposedMergedEntityJson;

    @Column(columnDefinition = "TEXT") // JSON representing the bot's reasoning (audit trail)
    private String reasoningJson;

    @Column(columnDefinition = "TEXT") // Comments from human review on approval/rejection
    private String reviewComment;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public MergeCandidatePair() {}

    public MergeCandidatePair(String entity1Json, String entity2Json, String proposedMergedEntityJson, String reasoningJson) {
        this.entity1Json = entity1Json;
        this.entity2Json = entity2Json;
        this.proposedMergedEntityJson = proposedMergedEntityJson;
        this.reasoningJson = reasoningJson;
        this.status = MergeStatus.PENDING_REVIEW;
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

    public MergeStatus getStatus() {
        return status;
    }

    public void setStatus(MergeStatus status) {
        this.status = status;
    }

    public String getProposedMergedEntityJson() {
        return proposedMergedEntityJson;
    }

    public void setProposedMergedEntityJson(String proposedMergedEntityJson) {
        this.proposedMergedEntityJson = proposedMergedEntityJson;
    }

    public String getReasoningJson() {
        return reasoningJson;
    }

    public void setReasoningJson(String reasoningJson) {
        this.reasoningJson = reasoningJson;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MergeCandidatePair that = (MergeCandidatePair) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "MergeCandidatePair{" +
                "id=" + id +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}