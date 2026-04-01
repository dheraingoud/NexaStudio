package com.nexastudio.project;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for File entity operations.
 */
@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {

    /**
     * Find all files by project ID
     */
    List<FileEntity> findByProjectIdOrderByPathAsc(UUID projectId);

    /**
     * Find file by project ID and path
     */
    Optional<FileEntity> findByProjectIdAndPath(UUID projectId, String path);

    /**
     * Check if file exists in project
     */
    boolean existsByProjectIdAndPath(UUID projectId, String path);

    /**
     * Delete all files in a project
     */
    @Modifying
    @Query("DELETE FROM FileEntity f WHERE f.project.id = :projectId")
    void deleteAllByProjectId(@Param("projectId") UUID projectId);

    /**
     * Find all code files in project
     */
    @Query("SELECT f FROM FileEntity f WHERE f.project.id = :projectId AND f.type = 'CODE' ORDER BY f.path")
    List<FileEntity> findCodeFilesByProjectId(@Param("projectId") UUID projectId);

    /**
     * Count files in project
     */
    long countByProjectId(UUID projectId);

    /**
     * Find files by extension
     */
    @Query("SELECT f FROM FileEntity f WHERE f.project.id = :projectId AND f.path LIKE %:extension")
    List<FileEntity> findByProjectIdAndExtension(@Param("projectId") UUID projectId,
            @Param("extension") String extension);
}
