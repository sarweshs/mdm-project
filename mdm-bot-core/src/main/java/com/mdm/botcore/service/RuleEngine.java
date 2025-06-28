package com.mdm.botcore.service;

import com.mdm.botcore.domain.model.MDMEntity;
import java.util.List;

public interface RuleEngine {
    /**
     * Processes a list of entities and returns merge suggestions.
     * @param entities The entities to process.
     * @param rules The rules (as DRL or Java, depending on engine).
     * @return List of MergeService.MergeSuggestion objects.
     */
    List<MergeService.MergeSuggestion> processEntities(List<MDMEntity> entities, List<String> rules);
} 