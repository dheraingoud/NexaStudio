package com.nexastudio.preview;

import com.nexastudio.common.ApiResponse;
import com.nexastudio.project.ProjectEntity;
import com.nexastudio.project.ProjectService;
import com.nexastudio.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Preview REST Controller.
 * Handles preview lifecycle operations.
 */
@RestController
@RequestMapping("/projects/{projectId}/preview")
public class PreviewController {

    private static final Logger log = LoggerFactory.getLogger(PreviewController.class);

    private final SandboxService sandboxService;
    private final ProjectService projectService;

    public PreviewController(SandboxService sandboxService, ProjectService projectService) {
        this.sandboxService = sandboxService;
        this.projectService = projectService;
    }

    /**
     * Start preview for a project
     * POST /api/projects/{projectId}/preview/start
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<SandboxService.PreviewStatus>> startPreview(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID projectId) {
        
        log.info("Start preview request for project: {}", projectId);
        
        ProjectEntity project = projectService.getProjectEntity(user.getId(), projectId);
        int port = sandboxService.allocatePort();
        
        sandboxService.startPreview(project, port);
        
        SandboxService.PreviewStatus status = sandboxService.getPreviewStatus(projectId);
        return ResponseEntity.ok(ApiResponse.success("Preview starting", status));
    }

    /**
     * Stop preview for a project
     * POST /api/projects/{projectId}/preview/stop
     */
    @PostMapping("/stop")
    public ResponseEntity<ApiResponse<Void>> stopPreview(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID projectId) {
        
        log.info("Stop preview request for project: {}", projectId);
        
        // Verify access
        projectService.getProjectEntity(user.getId(), projectId);
        
        sandboxService.stopPreview(projectId);
        return ResponseEntity.ok(ApiResponse.success("Preview stopped"));
    }

    /**
     * Get preview status
     * GET /api/projects/{projectId}/preview/status
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SandboxService.PreviewStatus>> getStatus(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID projectId) {
        
        // Verify access
        projectService.getProjectEntity(user.getId(), projectId);
        
        SandboxService.PreviewStatus status = sandboxService.getPreviewStatus(projectId);
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}
