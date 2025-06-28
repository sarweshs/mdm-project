package com.mdm.globalrules.domain.repository;

import com.mdm.globalrules.domain.model.GlobalMergeRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for managing GlobalMergeRule entities.
 */
@Repository
public interface GlobalMergeRuleRepository extends JpaRepository<GlobalMergeRule, Long> {

    /**
     * Finds global merge rules by domain and active status.
     * @param domain The domain (e.g., "lifescience").
     * @param active Whether the rule is active.
     * @return A list of matching GlobalMergeRule entities.
     */
    List<GlobalMergeRule> findByDomainAndActiveOrderByPriorityDesc(String domain, boolean active);

    /**
     * Finds a global merge rule by its name, regardless of domain.
     * Useful for checking if a company rule is attempting to override an existing global rule.
     * @param ruleName The name of the rule.
     * @return An Optional containing the GlobalMergeRule if found.
     */
    Optional<GlobalMergeRule> findByRuleName(String ruleName);
}