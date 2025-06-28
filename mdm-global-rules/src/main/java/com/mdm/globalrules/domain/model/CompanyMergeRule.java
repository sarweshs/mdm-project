package com.mdm.globalrules.domain.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a company-specific merge rule. Companies can override global rules
 * or add entirely new rules specific to their needs.
 */
@Entity
@Table(name = "company_merge_rules", uniqueConstraints = @UniqueConstraint(columnNames = {"companyId", "ruleName"}))
@EntityListeners(AuditingEntityListener.class)
public class CompanyMergeRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyId; // Identifier for the company/human agent

    @Column(nullable = false)
    private String ruleName; // Name of the rule. Can match a global rule name for override.

    @Column(length = 1000)
    private String description;

    @Lob
    @Column(nullable = false)
    private String ruleLogic; // DRL content specific to this company's rule

    @Column(nullable = false)
    private Integer priority; // Priority within company's rules, and for override resolution

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean overrideGlobal = false; // If true, this rule overrides a global rule with the same name

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Constructors
    public CompanyMergeRule() {
    }

    public CompanyMergeRule(String companyId, String ruleName, String description, String ruleLogic, Integer priority, boolean active, boolean overrideGlobal) {
        this.companyId = companyId;
        this.ruleName = ruleName;
        this.description = description;
        this.ruleLogic = ruleLogic;
        this.priority = priority;
        this.active = active;
        this.overrideGlobal = overrideGlobal;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
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

    public boolean isOverrideGlobal() {
        return overrideGlobal;
    }

    public void setOverrideGlobal(boolean overrideGlobal) {
        this.overrideGlobal = overrideGlobal;
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
        CompanyMergeRule that = (CompanyMergeRule) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CompanyMergeRule{" +
                "id=" + id +
                ", companyId='" + companyId + '\'' +
                ", ruleName='" + ruleName + '\'' +
                ", priority=" + priority +
                ", active=" + active +
                ", overrideGlobal=" + overrideGlobal +
                '}';
    }
}