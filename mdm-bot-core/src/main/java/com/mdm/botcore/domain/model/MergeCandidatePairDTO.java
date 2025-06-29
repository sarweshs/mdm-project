package com.mdm.botcore.domain.model;

import java.time.LocalDateTime;

public class MergeCandidatePairDTO {
    public Long id;
    public String status;
    public String entity1json;
    public String entity2json;
    public String proposedMergedEntityJson;
    public String reasoningJson;
    public String reviewComment;
    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static MergeCandidatePairDTO fromEntity(MergeCandidatePair pair) {
        MergeCandidatePairDTO dto = new MergeCandidatePairDTO();
        dto.id = pair.getId();
        dto.status = pair.getStatus() != null ? pair.getStatus().name() : null;
        dto.entity1json = pair.getEntity1Json();
        dto.entity2json = pair.getEntity2Json();
        dto.proposedMergedEntityJson = pair.getProposedMergedEntityJson();
        dto.reasoningJson = pair.getReasoningJson();
        dto.reviewComment = pair.getReviewComment();
        dto.createdAt = pair.getCreatedAt();
        dto.updatedAt = pair.getUpdatedAt();
        return dto;
    }
} 