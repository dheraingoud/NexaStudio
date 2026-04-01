package com.nexastudio.project.dto;

import com.nexastudio.project.FileEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for file data transfer.
 */
public class FileDTO {
    
    private UUID id;
    private String path;
    private String name;
    private String content;
    private String mimeType;
    private String type;
    private boolean generated;
    private boolean locked;
    private Long size;
    private Instant createdAt;
    private Instant updatedAt;

    public FileDTO() {}

    public FileDTO(UUID id, String path, String name, String content, String mimeType,
                   String type, boolean generated, boolean locked, Long size,
                   Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.content = content;
        this.mimeType = mimeType;
        this.type = type;
        this.generated = generated;
        this.locked = locked;
        this.size = size;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isGenerated() { return generated; }
    public void setGenerated(boolean generated) { this.generated = generated; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // Builder
    public static FileDTOBuilder builder() { return new FileDTOBuilder(); }

    public static class FileDTOBuilder {
        private UUID id;
        private String path;
        private String name;
        private String content;
        private String mimeType;
        private String type;
        private boolean generated;
        private boolean locked;
        private Long size;
        private Instant createdAt;
        private Instant updatedAt;

        public FileDTOBuilder id(UUID id) { this.id = id; return this; }
        public FileDTOBuilder path(String path) { this.path = path; return this; }
        public FileDTOBuilder name(String name) { this.name = name; return this; }
        public FileDTOBuilder content(String content) { this.content = content; return this; }
        public FileDTOBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public FileDTOBuilder type(String type) { this.type = type; return this; }
        public FileDTOBuilder generated(boolean generated) { this.generated = generated; return this; }
        public FileDTOBuilder locked(boolean locked) { this.locked = locked; return this; }
        public FileDTOBuilder size(Long size) { this.size = size; return this; }
        public FileDTOBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public FileDTOBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public FileDTO build() {
            return new FileDTO(id, path, name, content, mimeType, type, generated, locked, size, createdAt, updatedAt);
        }
    }
    
    /**
     * Convert entity to DTO
     */
    public static FileDTO fromEntity(FileEntity entity) {
        return FileDTO.builder()
                .id(entity.getId())
                .path(entity.getPath())
                .name(entity.getName())
                .content(entity.getContent())
                .mimeType(entity.getMimeType())
                .type(entity.getType().name())
                .generated(entity.isGenerated())
                .locked(entity.isLocked())
                .size(entity.getSize())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    /**
     * Convert entity to DTO without content (for listings)
     */
    public static FileDTO fromEntityWithoutContent(FileEntity entity) {
        return FileDTO.builder()
                .id(entity.getId())
                .path(entity.getPath())
                .name(entity.getName())
                .mimeType(entity.getMimeType())
                .type(entity.getType().name())
                .generated(entity.isGenerated())
                .locked(entity.isLocked())
                .size(entity.getSize())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
