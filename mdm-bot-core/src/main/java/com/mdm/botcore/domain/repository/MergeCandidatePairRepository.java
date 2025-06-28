package com.mdm.botcore.domain.repository;

import com.mdm.botcore.domain.model.MergeCandidatePair;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for managing MergeCandidatePair entities.
 */
@Repository
public interface MergeCandidatePairRepository extends JpaRepository<MergeCandidatePair, Long> {

    /**
     * Finds merge candidate pairs by their status.
     * @param status The status to filter by (e.g., PENDING_REVIEW, APPROVED, REJECTED).
     * @return A list of matching MergeCandidatePair entities.
     */
    List<MergeCandidatePair> findByStatus(MergeCandidatePair.MergeStatus status);
}