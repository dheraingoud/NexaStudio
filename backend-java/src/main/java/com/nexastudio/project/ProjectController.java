package com.nexastudio.project;

import com.nexastudio.ai.OrchestratorService;
import com.nexastudio.common.ApiResponse;
import com.nexastudio.project.dto.*;
import com.nexastudio.user.UserEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.UUID;

/**
 * Project REST Controller.
 * Handles project CRUD, file operations, and AI generation requests.
 */
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private static final Logger log = LoggerFactory.getLogger(ProjectController.class);

    private final ProjectService projectService;
    private final OrchestratorService orchestratorService;
    private final ObjectMapper objectMapper;

    public ProjectController(ProjectService projectService, OrchestratorService orchestratorService, ObjectMapper objectMapper) {
        this.projectService = projectService;
        this.orchestratorService = orchestratorService;
        this.objectMapper = objectMapper;
    }

    /**
     * Create a new project
     * POST /api/projects
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProjectDTO>> createProject(
            @AuthenticationPrincipal UserEntity user,
            @Valid @RequestBody CreateProjectRequest request) {
        
        log.info("Create project request from user: {}", user.getId());
        ProjectDTO project = projectService.createProject(user.getId(), request);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Project created successfully", project));
    }

    /**
     * Get all projects for current user
     * GET /api/projects
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectDTO>>> getProjects(
            @AuthenticationPrincipal UserEntity user) {
        
        List<ProjectDTO> projects = projectService.getProjects(user.getId());
        return ResponseEntity.ok(ApiResponse.success(projects));
    }

    /**
     * Get a single project by ID
     * GET /api/projects/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> getProject(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id) {
        
        ProjectDTO project = projectService.getProjectWithFiles(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(project));
    }

    /**
     * Update a project
     * PUT /api/projects/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProjectDTO>> updateProject(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProjectRequest request) {
        
        ProjectDTO project = projectService.updateProject(user.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Project updated successfully", project));
    }

    /**
     * Delete a project
     * DELETE /api/projects/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id) {
        
        projectService.deleteProject(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Project deleted successfully"));
    }

    /**
     * Get all files in a project
     * GET /api/projects/{id}/files
     */
    @GetMapping("/{id}/files")
    public ResponseEntity<ApiResponse<List<FileDTO>>> getFiles(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id) {
        
        List<FileDTO> files = projectService.getProjectFiles(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    /**
     * Get prompt / chat history for a project
     * GET /api/projects/{id}/prompts
     */
    @GetMapping("/{id}/prompts")
    public ResponseEntity<ApiResponse<List<PromptDTO>>> getPrompts(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id) {
        
        List<PromptDTO> prompts = projectService.getPromptHistory(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(prompts));
    }

    /**
     * Get a specific file
     * GET /api/projects/{id}/files?path=/app/page.tsx
     */
    @GetMapping("/{id}/file")
    public ResponseEntity<ApiResponse<FileDTO>> getFile(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id,
            @RequestParam String path) {
        
        FileDTO file = projectService.getFile(user.getId(), id, path);
        return ResponseEntity.ok(ApiResponse.success(file));
    }

    /**
     * Update a file
     * PUT /api/projects/{id}/files
     */
    @PutMapping("/{id}/file")
    public ResponseEntity<ApiResponse<FileDTO>> updateFile(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id,
            @RequestParam String path,
            @RequestBody String content) {
        
        FileDTO file = projectService.updateFile(user.getId(), id, path, content);
        return ResponseEntity.ok(ApiResponse.success("File updated successfully", file));
    }

    /**
     * Delete a file
     * DELETE /api/projects/{id}/file?path=/components/Button.tsx
     */
    @DeleteMapping("/{id}/file")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id,
            @RequestParam String path) {
        
        projectService.deleteFile(user.getId(), id, path);
        return ResponseEntity.ok(ApiResponse.success("File deleted successfully"));
    }

    /**
     * Generate code using AI
     * POST /api/projects/{id}/generate
     */
    @PostMapping("/{id}/generate")
    public ResponseEntity<ApiResponse<GenerateResponse>> generate(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id,
            @Valid @RequestBody GenerateRequest request) {
        
        log.info("Generate request for project {} from user {}: {}", 
                id, user.getId(), request.getPrompt().substring(0, Math.min(50, request.getPrompt().length())));
        
        GenerateResponse response = orchestratorService.generate(user.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Code generated successfully", response));
    }

    /**
     * Stream code generation via SSE.
     * Emits events: thinking, file, complete, error
     * POST /api/projects/{id}/generate/stream
     */
    @PostMapping(value = "/{id}/generate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateStream(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID id,
            @Valid @RequestBody GenerateRequest request) {
        
        log.info("SSE generate request for project {} from user {}", id, user.getId());

        // 5 minutes timeout for long generations
        SseEmitter emitter = new SseEmitter(300_000L);

        // Capture the security context from the request thread so the SSE daemon
        // thread can set it before calling emitter.send() / emitter.complete().
        // Without this, the async dispatch back through the servlet filter chain
        // fails with AccessDeniedException because the context is lost.
        final SecurityContext securityContext = SecurityContextHolder.getContext();

        // Daemon thread so JVM shutdown isn't blocked by long AI calls
        Thread sseThread = new Thread(() -> {
            // Propagate security context to this thread
            SecurityContextHolder.setContext(securityContext);
            try {
                orchestratorService.generateStreaming(user.getId(), id, request, event -> {
                    try {
                        String json = objectMapper.writeValueAsString(event.data());
                        emitter.send(SseEmitter.event()
                                .name(event.type())
                                .data(json, MediaType.APPLICATION_JSON));
                    } catch (Exception e) {
                        log.warn("Failed to send SSE event: {}", e.getMessage());
                    }
                });
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE generation error: {}", e.getMessage());
                try {
                    String safeMsg = e.getMessage() != null
                            ? e.getMessage()
                            : "Unknown generation error";
                    String errorJson = objectMapper.writeValueAsString(java.util.Map.of("message", safeMsg));
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data(errorJson, MediaType.APPLICATION_JSON));
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            } finally {
                SecurityContextHolder.clearContext();
            }
        }, "sse-gen-" + id);
        sseThread.setDaemon(true);
        sseThread.start();

        // Handle client disconnect gracefully
        emitter.onCompletion(() -> log.debug("SSE stream completed for project {}", id));
        emitter.onTimeout(() -> {
            log.warn("SSE stream timed out for project {}", id);
            emitter.complete();
        });

        return emitter;
    }
}
