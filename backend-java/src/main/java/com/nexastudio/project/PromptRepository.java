package com.nexastudio.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Prompt entity operations.
 */
@Repository
public interface PromptRepository extends JpaRepository<PromptEntity, UUID> {

    /**
     * Find all prompts by project ID with pagination
     */
    Page<PromptEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);

    /**
     * Find recent prompts by project ID (descending)
     */
    List<PromptEntity> findTop10ByProjectIdOrderByCreatedAtDesc(UUID projectId);

    /**
     * Find recent prompts by project ID (ascending, for chat history)
     */
    List<PromptEntity> findTop50ByProjectIdOrderByCreatedAtAsc(UUID projectId);

    /**
     * Count prompts by project ID
     */
    long countByProjectId(UUID projectId);

    /**
     * Find pending prompts
     */
    List<PromptEntity> findByStatusOrderByCreatedAtAsc(PromptEntity.PromptStatus status);
}
