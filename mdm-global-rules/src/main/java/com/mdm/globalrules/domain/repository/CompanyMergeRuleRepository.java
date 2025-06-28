package com.mdm.globalrules.domain.repository;

import com.mdm.globalrules.domain.model.CompanyMergeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for managing CompanyMergeRule entities.
 */
@Repository
public interface CompanyMergeRuleRepository extends JpaRepository<CompanyMergeRule, Long> {

    /**
     * Finds company merge rules for a specific company ID and active status, ordered by priority.
     * @param companyId The ID of the company.
     * @param active Whether the rule is active.
     * @return A list of matching CompanyMergeRule entities.
     */
    List<CompanyMergeRule> findByCompanyIdAndActiveOrderByPriorityDesc(String companyId, boolean active);

    /**
     * Finds a company merge rule by company ID and rule name.
     * @param companyId The ID of the company.
     * @param ruleName The name of the rule.
     * @return An Optional containing the CompanyMergeRule if found.
     */
    Optional<CompanyMergeRule> findByCompanyIdAndRuleName(String companyId, String ruleName);
}