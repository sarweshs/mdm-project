package com.mdm.botcore.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an audit log entry for a merge decision.
 * Captures the reasoning and rules applied by the bot.
 */
@Entity
@Table(name = "audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "merge_candidate_pair_id")
    private MergeCandidatePair mergeCandidatePair; // Link to the merge candidate

    @Column(nullable = false)
    private String ruleName; // Name of the rule that fired

    @Column(columnDefinition = "TEXT")
    private String ruleDetails; // Details about why the rule fired (e.g., field values matched)

    @Column(nullable = false)
    private String entity1Id; // ID of the first entity involved
    @Column(nullable = false)
    private String entity2Id; // ID of the second entity involved

    @Column(nullable = false)
    private boolean botDecisionToMerge; // True if the bot decided to merge based on this rule

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime timestamp;

    // Constructors
    public AuditLog() {}

    public AuditLog(MergeCandidatePair mergeCandidatePair, String ruleName, String ruleDetails, String entity1Id, String entity2Id, boolean botDecisionToMerge) {
        this.mergeCandidatePair = mergeCandidatePair;
        this.ruleName = ruleName;
        this.ruleDetails = ruleDetails;
        this.entity1Id = entity1Id;
        this.entity2Id = entity2Id;
        this.botDecisionToMerge = botDecisionToMerge;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MergeCandidatePair getMergeCandidatePair() {
        return mergeCandidatePair;
    }

    public void setMergeCandidatePair(MergeCandidatePair mergeCandidatePair) {
        this.mergeCandidatePair = mergeCandidatePair;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getRuleDetails() {
        return ruleDetails;
    }

    public void setRuleDetails(String ruleDetails) {
        this.ruleDetails = ruleDetails;
    }

    public String getEntity1Id() {
        return entity1Id;
    }

    public void setEntity1Id(String entity1Id) {
        this.entity1Id = entity1Id;
    }

    public String getEntity2Id() {
        return entity2Id;
    }

    public void setEntity2Id(String entity2Id) {
        this.entity2Id = entity2Id;
    }

    public boolean isBotDecisionToMerge() {
        return botDecisionToMerge;
    }

    public void setBotDecisionToMerge(boolean botDecisionToMerge) {
        this.botDecisionToMerge = botDecisionToMerge;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuditLog auditLog = (AuditLog) o;
        return Objects.equals(id, auditLog.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", ruleName='" + ruleName + '\'' +
                ", entity1Id='" + entity1Id + '\'' +
                ", entity2Id='" + entity2Id + '\'' +
                ", botDecisionToMerge=" + botDecisionToMerge +
                ", timestamp=" + timestamp +
                '}';
    }
}