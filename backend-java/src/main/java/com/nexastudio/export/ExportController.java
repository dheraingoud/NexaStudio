package com.nexastudio.export;

import com.nexastudio.common.ApiResponse;
import com.nexastudio.project.ProjectEntity;
import com.nexastudio.project.ProjectService;
import com.nexastudio.user.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

/**
 * Export REST Controller.
 * Handles project export operations.
 */
@RestController
@RequestMapping("/projects/{projectId}/export")
public class ExportController {

    private static final Logger log = LoggerFactory.getLogger(ExportController.class);

    private final ZipExportService exportService;
    private final ProjectService projectService;

    public ExportController(ZipExportService exportService, ProjectService projectService) {
        this.exportService = exportService;
        this.projectService = projectService;
    }

    /**
     * Create export and get download token
     * POST /api/projects/{projectId}/export
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ExportResponse>> createExport(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID projectId) throws IOException {
        
        log.info("Export request for project: {}", projectId);
        
        ProjectEntity project = projectService.getProjectEntity(user.getId(), projectId);
        String token = exportService.createExport(project);
        
        ExportResponse response = new ExportResponse(
                token,
                "/api/projects/" + projectId + "/export/download?token=" + token
        );
        
        return ResponseEntity.ok(ApiResponse.success("Export created", response));
    }

    /**
     * Download export by token
     * GET /api/projects/{projectId}/export/download?token=xxx
     */
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExport(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID projectId,
            @RequestParam String token) throws IOException {
        
        log.info("Download export request: {}", token);
        
        // Verify access
        projectService.getProjectEntity(user.getId(), projectId);
        
        ZipExportService.ExportResult result = exportService.getExport(token);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + result.filename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(result.data().length)
                .body(result.data());
    }

    /**
     * Direct download (creates and downloads in one request)
     * GET /api/projects/{projectId}/export/direct
     */
    @GetMapping("/direct")
    public ResponseEntity<byte[]> directDownload(
            @AuthenticationPrincipal UserEntity user,
            @PathVariable UUID projectId) throws IOException {
        
        log.info("Direct download request for project: {}", projectId);
        
        ProjectEntity project = projectService.getProjectEntity(user.getId(), projectId);
        byte[] zipData = exportService.exportProject(project);
        
        String filename = project.getName().replaceAll("[^a-zA-Z0-9-_]", "-") + ".zip";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(zipData.length)
                .body(zipData);
    }

    /**
     * Export response DTO
     */
    public record ExportResponse(String token, String downloadUrl) {}
}
