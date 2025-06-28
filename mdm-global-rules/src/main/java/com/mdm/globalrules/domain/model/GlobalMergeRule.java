package com.mdm.globalrules.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a global merge rule applicable across the MDM domain (e.g., 'lifescience').
 * These rules define the default behavior for merging entities.
 */
@Entity
@Table(name = "global_merge_rules")
@EntityListeners(AuditingEntityListener.class) // Enable auditing for automatic timestamp management
public class GlobalMergeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String domain; // e.g., "lifescience"

    @Column(nullable = false, unique = true)
    private String ruleName; // e.g., "ExactMatchCompanyName"

    @Column(length = 1000) // Allow for longer descriptions
    private String description;

    @Lob // Store as a large object, suitable for DRL content
    @Column(nullable = false)
    private String ruleLogic; // DRL content for the rule

    @Column(nullable = false)
    private Integer priority; // Higher number means higher priority

    @Column(nullable = false)
    private boolean active = true; // Whether the rule is currently active

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public GlobalMergeRule() {
    }

    public GlobalMergeRule(String domain, String ruleName, String description, String ruleLogic, Integer priority, boolean active) {
        this.domain = domain;
        this.ruleName = ruleName;
        this.description = description;
        this.ruleLogic = ruleLogic;
        this.priority = priority;
        this.active = active;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleLogic() {
        return ruleLogic;
    }

    public void setRuleLogic(String ruleLogic) {
        this.ruleLogic = ruleLogic;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
        GlobalMergeRule that = (GlobalMergeRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "GlobalMergeRule{" +
                "id=" + id +
                ", domain='" + domain + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", priority=" + priority +
                ", active=" + active +
                '}';
    }
}