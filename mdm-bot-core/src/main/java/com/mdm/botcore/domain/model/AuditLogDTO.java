package com.mdm.botcore.domain.model;

import java.time.LocalDateTime;

public class AuditLogDTO {
    private Long id;
    private String ruleName;
    private String ruleDetails;
    private String entity1Id;
    private String entity2Id;
    private boolean botDecisionToMerge;
    private LocalDateTime timestamp;

    public AuditLogDTO() {}

    public AuditLogDTO(Long id, String ruleName, String ruleDetails, String entity1Id, String entity2Id, boolean botDecisionToMerge, LocalDateTime timestamp) {
        this.id = id;
        this.ruleName = ruleName;
        this.ruleDetails = ruleDetails;
        this.entity1Id = entity1Id;
        this.entity2Id = entity2Id;
        this.botDecisionToMerge = botDecisionToMerge;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleName() { return ruleName; }
    public void setRuleName(String ruleName) { this.ruleName = ruleName; }
    public String getRuleDetails() { return ruleDetails; }
    public void setRuleDetails(String ruleDetails) { this.ruleDetails = ruleDetails; }
    public String getEntity1Id() { return entity1Id; }
    public void setEntity1Id(String entity1Id) { this.entity1Id = entity1Id; }
    public String getEntity2Id() { return entity2Id; }
    public void setEntity2Id(String entity2Id) { this.entity2Id = entity2Id; }
    public boolean isBotDecisionToMerge() { return botDecisionToMerge; }
    public void setBotDecisionToMerge(boolean botDecisionToMerge) { this.botDecisionToMerge = botDecisionToMerge; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
} 