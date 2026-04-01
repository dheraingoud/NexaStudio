package com.nexastudio.export;

import com.nexastudio.common.exception.NexaStudioException;
import com.nexastudio.project.FileEntity;
import com.nexastudio.project.FileRepository;
import com.nexastudio.project.ProjectEntity;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ZIP Export Service.
 * Handles exporting projects as downloadable ZIP files.
 */
@Service
public class ZipExportService {

    private static final Logger log = LoggerFactory.getLogger(ZipExportService.class);

    private final FileRepository fileRepository;

    public ZipExportService(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    @Value("${storage.base-path}")
    private String storagePath;

    // Track export files with expiration
    private final Map<String, ExportInfo> exports = new ConcurrentHashMap<>();

    /**
     * Export project as ZIP
     */
    @Transactional(readOnly = true)
    public byte[] exportProject(ProjectEntity project) throws IOException {
        log.info("Exporting project: {}", project.getId());

        List<FileEntity> files = fileRepository.findByProjectIdOrderByPathAsc(project.getId());

        if (files.isEmpty()) {
            throw new NexaStudioException("EXPORT_ERROR", "No files to export");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipArchiveOutputStream zos = new ZipArchiveOutputStream(baos)) {
            String rootFolder = sanitizeName(project.getName()) + "/";

            for (FileEntity file : files) {
                if (file.getContent() == null)
                    continue;

                String entryPath = file.getPath();
                if (entryPath.startsWith("/")) {
                    entryPath = entryPath.substring(1);
                }
                entryPath = rootFolder + entryPath;

                ZipArchiveEntry entry = new ZipArchiveEntry(entryPath);
                byte[] content = file.getContent().getBytes(StandardCharsets.UTF_8);
                entry.setSize(content.length);

                zos.putArchiveEntry(entry);
                zos.write(content);
                zos.closeArchiveEntry();
            }
        }

        log.info("Export completed: {} files, {} bytes", files.size(), baos.size());
        return baos.toByteArray();
    }

    /**
     * Create export and return download token
     */
    @Transactional(readOnly = true)
    public String createExport(ProjectEntity project) throws IOException {
        byte[] zipData = exportProject(project);

        String token = UUID.randomUUID().toString();
        Path exportPath = Paths.get(storagePath, "exports", token + ".zip");

        Files.createDirectories(exportPath.getParent());
        Files.write(exportPath, zipData);

        ExportInfo info = new ExportInfo(
                project.getId(),
                exportPath,
                Instant.now().plusSeconds(3600), // 1 hour expiration
                sanitizeName(project.getName()) + ".zip");
        exports.put(token, info);

        log.info("Export created with token: {}", token);
        return token;
    }

    /**
     * Get export by token
     */
    public ExportResult getExport(String token) throws IOException {
        ExportInfo info = exports.get(token);

        if (info == null) {
            throw new NexaStudioException("EXPORT_NOT_FOUND", "Export not found or expired");
        }

        if (Instant.now().isAfter(info.expiresAt)) {
            exports.remove(token);
            deleteExportFile(info.path);
            throw new NexaStudioException("EXPORT_EXPIRED", "Export has expired");
        }

        byte[] data = Files.readAllBytes(info.path);
        return new ExportResult(data, info.filename);
    }

    /**
     * Clean up expired exports
     */
    public void cleanupExpiredExports() {
        Instant now = Instant.now();
        exports.entrySet().removeIf(entry -> {
            if (now.isAfter(entry.getValue().expiresAt)) {
                deleteExportFile(entry.getValue().path);
                return true;
            }
            return false;
        });
    }

    /**
     * Delete export file
     */
    private void deleteExportFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.error("Failed to delete export file: {}", path, e);
        }
    }

    /**
     * Sanitize project name for filename
     */
    private String sanitizeName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "-")
                .replaceAll("-+", "-")
                .toLowerCase();
    }

    /**
     * Export info holder
     */
    private record ExportInfo(UUID projectId, Path path, Instant expiresAt, String filename) {
    }

    /**
     * Export result holder
     */
    public record ExportResult(byte[] data, String filename) {
    }
}
