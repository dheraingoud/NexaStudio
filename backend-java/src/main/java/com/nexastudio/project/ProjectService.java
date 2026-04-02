package com.nexastudio.project;

import com.nexastudio.common.Constants;
import com.nexastudio.common.exception.ResourceNotFoundException;
import com.nexastudio.common.exception.ValidationException;
import com.nexastudio.generator.FrameworkScaffoldService;
import com.nexastudio.project.dto.*;
import com.nexastudio.user.UserEntity;
import com.nexastudio.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Project operations.
 * Handles project CRUD, file management, and coordinates with AI generation.
 */
@Service
public class ProjectService {

    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);

    private final ProjectRepository projectRepository;
    private final FileRepository fileRepository;
    private final PromptRepository promptRepository;
    private final UserService userService;
    private final FrameworkScaffoldService scaffoldService;

    public ProjectService(ProjectRepository projectRepository, FileRepository fileRepository,
                          PromptRepository promptRepository, UserService userService,
                          FrameworkScaffoldService scaffoldService) {
        this.projectRepository = projectRepository;
        this.fileRepository = fileRepository;
        this.promptRepository = promptRepository;
        this.userService = userService;
        this.scaffoldService = scaffoldService;
    }

    /**
     * Create a new project
     */
    @Transactional
    public ProjectDTO createProject(UUID userId, CreateProjectRequest request) {
        log.info("Creating project '{}' for user {}", request.getName(), userId);
        
        UserEntity user = userService.findById(userId);
        
        // Check project limit
        long projectCount = projectRepository.countByUserIdAndStatus(userId, ProjectEntity.ProjectStatus.ACTIVE);
        int limit = userService.getProjectLimit(user);
        
        if (projectCount >= limit) {
            throw new ValidationException(
                    "Project limit reached. You can have maximum " + limit + " active projects.",
                    Constants.ERR_PROJECT_LIMIT
            );
        }
        
        // Determine project type
        ProjectEntity.ProjectType type = ProjectEntity.ProjectType.NEXTJS;
        if (request.getType() != null) {
            try {
                type = ProjectEntity.ProjectType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid project type: {}, defaulting to NEXTJS", request.getType());
            }
        }
        
        // Create project entity
        ProjectEntity project = ProjectEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .user(user)
                .type(type)
                .status(ProjectEntity.ProjectStatus.ACTIVE)
                .build();
        
        project = projectRepository.save(project);
        
        // Create initial scaffold
        List<FileEntity> scaffoldFiles = scaffoldService.createScaffold(project);
        scaffoldFiles.forEach(project::addFile);
        
        project = projectRepository.save(project);
        
        log.info("Project created successfully: {}", project.getId());
        return ProjectDTO.fromEntity(project);
    }

    /**
     * Get all projects for a user
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjects(UUID userId) {
        return projectRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(
                        userId, ProjectEntity.ProjectStatus.ACTIVE)
                .stream()
                .map(ProjectDTO::fromEntitySummary)
                .collect(Collectors.toList());
    }

    /**
     * Get projects with pagination
     */
    @Transactional(readOnly = true)
    public Page<ProjectDTO> getProjects(UUID userId, Pageable pageable) {
        return projectRepository.findByUserIdAndStatusOrderByUpdatedAtDesc(
                        userId, ProjectEntity.ProjectStatus.ACTIVE, pageable)
                .map(ProjectDTO::fromEntitySummary);
    }

    /**
     * Get a single project by ID
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProject(UUID userId, UUID projectId) {
        ProjectEntity project = findProjectForUser(userId, projectId);
        return ProjectDTO.fromEntity(project);
    }

    /**
     * Get project with all files
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectWithFiles(UUID userId, UUID projectId) {
        ProjectEntity project = projectRepository.findByIdWithFiles(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        return ProjectDTO.fromEntity(project);
    }

    /**
     * Update a project
     */
    @Transactional
    public ProjectDTO updateProject(UUID userId, UUID projectId, UpdateProjectRequest request) {
        ProjectEntity project = findProjectForUser(userId, projectId);
        
        if (request.getName() != null) {
            project.setName(request.getName());
        }
        if (request.getDescription() != null) {
            project.setDescription(request.getDescription());
        }
        
        project = projectRepository.save(project);
        log.info("Project updated: {}", projectId);
        
        return ProjectDTO.fromEntity(project);
    }

    /**
     * Delete a project (soft delete)
     */
    @Transactional
    public void deleteProject(UUID userId, UUID projectId) {
        ProjectEntity project = findProjectForUser(userId, projectId);
        project.setStatus(ProjectEntity.ProjectStatus.DELETED);
        projectRepository.save(project);
        log.info("Project deleted: {}", projectId);
    }

    /**
     * Get all files in a project (with content for AI generation context)
     */
    @Transactional(readOnly = true)
    public List<FileDTO> getProjectFiles(UUID userId, UUID projectId) {
        verifyProjectAccess(userId, projectId);
        return fileRepository.findByProjectIdOrderByPathAsc(projectId)
                .stream()
                .map(FileDTO::fromEntity)  // Include content for frontend code viewing
                .collect(Collectors.toList());
    }

    /**
     * Get a specific file with content
     */
    @Transactional(readOnly = true)
    public FileDTO getFile(UUID userId, UUID projectId, String path) {
        verifyProjectAccess(userId, projectId);
        FileEntity file = fileRepository.findByProjectIdAndPath(projectId, path)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + path));
        return FileDTO.fromEntity(file);
    }

    /**
     * Update a file's content
     */
    @Transactional
    public FileDTO updateFile(UUID userId, UUID projectId, String path, String content) {
        verifyProjectAccess(userId, projectId);
        
        FileEntity file = fileRepository.findByProjectIdAndPath(projectId, path)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + path));
        
        if (file.isLocked()) {
            throw new ValidationException("File is locked and cannot be modified: " + path);
        }
        
        file.setContent(content);
        file.setSize((long) content.length());
        file = fileRepository.save(file);
        
        return FileDTO.fromEntity(file);
    }

    /**
     * Create or update a file
     */
    @Transactional
    public FileDTO saveFile(UUID userId, UUID projectId, String path, String content, boolean generated) {
        ProjectEntity project = findProjectForUser(userId, projectId);
        
        FileEntity file = fileRepository.findByProjectIdAndPath(projectId, path)
                .orElse(FileEntity.builder()
                        .project(project)
                        .path(path)
                        .build());
        
        file.setContent(content);
        file.setGenerated(generated);
        file = fileRepository.save(file);
        
        // Update project timestamp
        project.setUpdatedAt(Instant.now());
        if (generated) {
            project.setLastGeneratedAt(Instant.now());
        }
        projectRepository.save(project);
        
        return FileDTO.fromEntity(file);
    }

    /**
     * Delete a file
     */
    @Transactional
    public void deleteFile(UUID userId, UUID projectId, String path) {
        verifyProjectAccess(userId, projectId);
        
        FileEntity file = fileRepository.findByProjectIdAndPath(projectId, path)
                .orElseThrow(() -> new ResourceNotFoundException("File not found: " + path));
        
        if (file.isLocked()) {
            throw new ValidationException("File is locked and cannot be deleted: " + path);
        }
        
        fileRepository.delete(file);
    }

    /**
     * Get the project entity for internal use
     */
    @Transactional(readOnly = true)
    public ProjectEntity getProjectEntity(UUID userId, UUID projectId) {
        return findProjectForUser(userId, projectId);
    }

    /**
     * Save prompt to project
     */
    @Transactional
    public PromptEntity savePrompt(ProjectEntity project, String text, PromptEntity.PromptIntent intent) {
        PromptEntity prompt = PromptEntity.builder()
                .project(project)
                .text(text)
                .intent(intent)
                .status(PromptEntity.PromptStatus.PENDING)
                .build();
        
        return promptRepository.save(prompt);
    }

    /**
     * Update prompt status
     */
    @Transactional
    public void updatePromptStatus(UUID promptId, PromptEntity.PromptStatus status, 
                                    String response, Integer tokensUsed, Long processingTime, String error) {
        PromptEntity prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt", promptId));
        
        prompt.setStatus(status);
        prompt.setAiResponse(response);
        prompt.setTokensUsed(tokensUsed);
        prompt.setProcessingTimeMs(processingTime);
        prompt.setErrorMessage(error);
        
        if (status == PromptEntity.PromptStatus.COMPLETED || status == PromptEntity.PromptStatus.FAILED) {
            prompt.setCompletedAt(Instant.now());
        }
        
        promptRepository.save(prompt);
    }

    /**
     * Get prompt / chat history for a project (chronological order).
     * Returns up to 50 most recent prompts.
     */
    @Transactional(readOnly = true)
    public List<com.nexastudio.project.dto.PromptDTO> getPromptHistory(UUID userId, UUID projectId) {
        verifyProjectAccess(userId, projectId);
        List<PromptEntity> prompts = promptRepository.findTop50ByProjectIdOrderByCreatedAtAsc(projectId);
        return prompts.stream()
                .map(com.nexastudio.project.dto.PromptDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to find project and verify user access
     */
    private ProjectEntity findProjectForUser(UUID userId, UUID projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        
        // Null-safety check for project owner
        if (project.getUser() == null || project.getUser().getId() == null) {
            log.error("Project {} has null owner — data integrity issue", projectId);
            throw new org.springframework.security.access.AccessDeniedException(
                    "Project owner data is corrupt. Please contact support.");
        }
        
        if (!project.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied to this project.");
        }
        
        return project;
    }

    /**
     * Helper method to verify user has access to project
     */
    private void verifyProjectAccess(UUID userId, UUID projectId) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));
        
        // Null-safety check for project owner
        if (project.getUser() == null || project.getUser().getId() == null) {
            log.error("Project {} has null owner — data integrity issue", projectId);
            throw new org.springframework.security.access.AccessDeniedException(
                    "Project owner data is corrupt. Please contact support.");
        }
        
        if (!project.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Access denied to this project.");
        }
    }
}
