package com.nexastudio.preview;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.nexastudio.common.exception.NexaStudioException;
import com.nexastudio.project.FileEntity;
import com.nexastudio.project.FileRepository;
import com.nexastudio.project.ProjectEntity;

/**
 * Sandbox Service for running project previews.
 * Manages file system workspace and preview lifecycle.
 */
@Service
public class SandboxService {

    private static final Logger log = LoggerFactory.getLogger(SandboxService.class);

    private final FileRepository fileRepository;
    private final ProcessManager processManager;

    public SandboxService(FileRepository fileRepository, ProcessManager processManager) {
        this.fileRepository = fileRepository;
        this.processManager = processManager;
    }

    @Value("${storage.base-path}")
    private String storagePath;

    @Value("${preview.base-port}")
    private int basePort;

    @Value("${preview.max-instances}")
    private int maxInstances;

    // Track active sandboxes
    private final Map<UUID, SandboxInfo> activeSandboxes = new ConcurrentHashMap<>();

    /**
     * Create sandbox workspace for a project
     */
    public Path createWorkspace(ProjectEntity project) throws IOException {
        Path workspacePath = Paths.get(storagePath, "projects", project.getId().toString());
        
        // Clean up existing workspace
        if (Files.exists(workspacePath)) {
            deleteDirectory(workspacePath);
        }
        
        // Create directory
        Files.createDirectories(workspacePath);
        
        // Write all project files
        List<FileEntity> files = fileRepository.findByProjectIdOrderByPathAsc(project.getId());
        for (FileEntity file : files) {
            writeFile(workspacePath, file);
        }
        
        log.info("Created workspace at: {}", workspacePath);
        return workspacePath;
    }

    /**
     * Write a file to the workspace
     */
    private void writeFile(Path workspacePath, FileEntity file) throws IOException {
        if (file.getContent() == null) return;
        
        String relativePath = file.getPath();
        if (relativePath.startsWith("/")) {
            relativePath = relativePath.substring(1);
        }

        // Block path traversal
        if (relativePath.contains("..")) {
            log.warn("Blocked path traversal attempt: {}", relativePath);
            return;
        }
        
        Path filePath = workspacePath.resolve(relativePath).normalize();
        // Ensure resolved path stays within the workspace
        if (!filePath.startsWith(workspacePath)) {
            log.warn("Blocked file write outside workspace: {}", filePath);
            return;
        }
        Files.createDirectories(filePath.getParent());
        Files.writeString(filePath, file.getContent());
    }

    /**
     * Start a preview for a project
     */
    @Async("previewTaskExecutor")
    public void startPreview(ProjectEntity project, int port) {
        UUID projectId = project.getId();
        
        try {
            // Check if already running
            if (activeSandboxes.containsKey(projectId)) {
                log.info("Preview already running for project: {}", projectId);
                return;
            }
            
            // Check max instances
            if (activeSandboxes.size() >= maxInstances) {
                throw new NexaStudioException("PREVIEW_LIMIT", 
                        "Maximum preview instances reached");
            }
            
            // Create workspace
            Path workspace = createWorkspace(project);
            
            // Store sandbox info
            SandboxInfo info = new SandboxInfo(projectId, workspace, port);
            activeSandboxes.put(projectId, info);
            
            // Note: Actual npm install and dev server would run here
            // For now, we just prepare the workspace
            log.info("Preview workspace prepared for project {} at port {}", projectId, port);
            
        } catch (Exception e) {
            log.error("Failed to start preview for project {}: {}", projectId, e.getMessage());
            activeSandboxes.remove(projectId);
            throw new NexaStudioException("PREVIEW_ERROR", 
                    "Failed to start preview: " + e.getMessage());
        }
    }

    /**
     * Stop a preview
     */
    public void stopPreview(UUID projectId) {
        SandboxInfo info = activeSandboxes.remove(projectId);
        if (info != null) {
            processManager.stopProcess(projectId);
            log.info("Stopped preview for project: {}", projectId);
        }
    }

    /**
     * Get preview status
     */
    public PreviewStatus getPreviewStatus(UUID projectId) {
        SandboxInfo info = activeSandboxes.get(projectId);
        if (info == null) {
            return new PreviewStatus(projectId, "STOPPED", null, null);
        }
        
        return new PreviewStatus(
                projectId,
                "RUNNING",
                info.port,
                "http://localhost:" + info.port
        );
    }

    /**
     * Allocate a port for preview
     */
    public int allocatePort() {
        for (int port = basePort; port < basePort + 100; port++) {
            final int checkPort = port;
            boolean inUse = activeSandboxes.values().stream()
                    .anyMatch(s -> s.port == checkPort);
            if (!inUse && isPortAvailable(port)) {
                return port;
            }
        }
        throw new NexaStudioException("NO_PORTS", "No available ports for preview");
    }

    /**
     * Check if a port is available
     */
    private boolean isPortAvailable(int port) {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Delete directory recursively
     */
    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                    .sorted((a, b) -> b.compareTo(a))
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }

    /**
     * Clean up old workspaces
     */
    public void cleanupOldWorkspaces() {
        // Implementation for scheduled cleanup
        log.debug("Cleaning up old workspaces");
    }

    /**
     * Sandbox info holder
     */
    private record SandboxInfo(UUID projectId, Path workspace, int port) {}

    /**
     * Preview status DTO
     */
    public record PreviewStatus(UUID projectId, String status, Integer port, String url) {}
}
