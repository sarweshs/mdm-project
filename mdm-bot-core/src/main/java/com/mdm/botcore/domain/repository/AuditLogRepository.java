package com.mdm.botcore.domain.repository;

import com.mdm.botcore.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * JPA Repository for managing AuditLog entities.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Finds audit logs associated with a specific merge candidate pair ID.
     * @param mergeCandidatePairId The ID of the merge candidate pair.
     * @return A list of matching AuditLog entries.
     */
    List<AuditLog> findByMergeCandidatePairId(Long mergeCandidatePairId);
}