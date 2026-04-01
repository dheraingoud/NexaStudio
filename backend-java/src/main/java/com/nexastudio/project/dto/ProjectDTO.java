package com.nexastudio.project.dto;

import com.nexastudio.project.ProjectEntity;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO for project data transfer.
 */
public class ProjectDTO {
    
    private UUID id;
    private String name;
    private String description;
    private String status;
    private String type;
    private String previewUrl;
    private Integer previewPort;
    private List<FileDTO> files;
    private int fileCount;
    private int promptCount;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastGeneratedAt;

    public ProjectDTO() {}

    public ProjectDTO(UUID id, String name, String description, String status, String type,
                      String previewUrl, Integer previewPort, List<FileDTO> files,
                      int fileCount, int promptCount, Instant createdAt, Instant updatedAt,
                      Instant lastGeneratedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.type = type;
        this.previewUrl = previewUrl;
        this.previewPort = previewPort;
        this.files = files;
        this.fileCount = fileCount;
        this.promptCount = promptCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastGeneratedAt = lastGeneratedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public Integer getPreviewPort() { return previewPort; }
    public void setPreviewPort(Integer previewPort) { this.previewPort = previewPort; }

    public List<FileDTO> getFiles() { return files; }
    public void setFiles(List<FileDTO> files) { this.files = files; }

    public int getFileCount() { return fileCount; }
    public void setFileCount(int fileCount) { this.fileCount = fileCount; }

    public int getPromptCount() { return promptCount; }
    public void setPromptCount(int promptCount) { this.promptCount = promptCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getLastGeneratedAt() { return lastGeneratedAt; }
    public void setLastGeneratedAt(Instant lastGeneratedAt) { this.lastGeneratedAt = lastGeneratedAt; }

    // Builder
    public static ProjectDTOBuilder builder() { return new ProjectDTOBuilder(); }

    public static class ProjectDTOBuilder {
        private UUID id;
        private String name;
        private String description;
        private String status;
        private String type;
        private String previewUrl;
        private Integer previewPort;
        private List<FileDTO> files;
        private int fileCount;
        private int promptCount;
        private Instant createdAt;
        private Instant updatedAt;
        private Instant lastGeneratedAt;

        public ProjectDTOBuilder id(UUID id) { this.id = id; return this; }
        public ProjectDTOBuilder name(String name) { this.name = name; return this; }
        public ProjectDTOBuilder description(String description) { this.description = description; return this; }
        public ProjectDTOBuilder status(String status) { this.status = status; return this; }
        public ProjectDTOBuilder type(String type) { this.type = type; return this; }
        public ProjectDTOBuilder previewUrl(String previewUrl) { this.previewUrl = previewUrl; return this; }
        public ProjectDTOBuilder previewPort(Integer previewPort) { this.previewPort = previewPort; return this; }
        public ProjectDTOBuilder files(List<FileDTO> files) { this.files = files; return this; }
        public ProjectDTOBuilder fileCount(int fileCount) { this.fileCount = fileCount; return this; }
        public ProjectDTOBuilder promptCount(int promptCount) { this.promptCount = promptCount; return this; }
        public ProjectDTOBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public ProjectDTOBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public ProjectDTOBuilder lastGeneratedAt(Instant lastGeneratedAt) { this.lastGeneratedAt = lastGeneratedAt; return this; }

        public ProjectDTO build() {
            return new ProjectDTO(id, name, description, status, type, previewUrl, previewPort,
                    files, fileCount, promptCount, createdAt, updatedAt, lastGeneratedAt);
        }
    }

    /**
     * Convert entity to DTO with files
     */
    public static ProjectDTO fromEntity(ProjectEntity entity) {
        return ProjectDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .type(entity.getType().name())
                .previewUrl(entity.getPreviewUrl())
                .previewPort(entity.getPreviewPort())
                .files(entity.getFiles().stream()
                        .map(FileDTO::fromEntityWithoutContent)
                        .collect(Collectors.toList()))
                .fileCount(entity.getFiles().size())
                .promptCount(entity.getPrompts().size())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastGeneratedAt(entity.getLastGeneratedAt())
                .build();
    }
    
    /**
     * Convert entity to DTO without files (for listings)
     */
    public static ProjectDTO fromEntitySummary(ProjectEntity entity) {
        return ProjectDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .status(entity.getStatus().name())
                .type(entity.getType().name())
                .previewUrl(entity.getPreviewUrl())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .lastGeneratedAt(entity.getLastGeneratedAt())
                .build();
    }
}
