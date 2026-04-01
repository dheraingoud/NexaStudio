package com.nexastudio.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Project entity operations.
 */
@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {

        /**
         * Find all projects by user ID with pagination
         */
        Page<ProjectEntity> findByUserIdAndStatusOrderByUpdatedAtDesc(
                        UUID userId, ProjectEntity.ProjectStatus status, Pageable pageable);

        /**
         * Find all active projects by user ID
         */
        List<ProjectEntity> findByUserIdAndStatusOrderByUpdatedAtDesc(
                        UUID userId, ProjectEntity.ProjectStatus status);

        /**
         * Find project by ID and user ID
         */
        Optional<ProjectEntity> findByIdAndUserId(UUID id, UUID userId);

        /**
         * Count projects by user ID
         */
        long countByUserIdAndStatus(UUID userId, ProjectEntity.ProjectStatus status);

        /**
         * Check if project exists and belongs to user
         */
        boolean existsByIdAndUserId(UUID id, UUID userId);

        /**
         * Find project with files
         */
        @Query("SELECT DISTINCT p FROM ProjectEntity p LEFT JOIN FETCH p.files WHERE p.id = :id AND p.user.id = :userId")
        Optional<ProjectEntity> findByIdWithFiles(@Param("id") UUID id, @Param("userId") UUID userId);

        /**
         * Find project with prompts
         */
        @Query("SELECT DISTINCT p FROM ProjectEntity p LEFT JOIN FETCH p.prompts WHERE p.id = :id AND p.user.id = :userId")
        Optional<ProjectEntity> findByIdWithPrompts(@Param("id") UUID id, @Param("userId") UUID userId);
}
