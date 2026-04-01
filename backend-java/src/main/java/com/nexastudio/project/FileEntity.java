package com.nexastudio.project;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * File entity representing a file within a project.
 * Stores file path and content with versioning support.
 */
@Entity
@Table(name = "project_files", indexes = {
    @Index(name = "idx_file_project", columnList = "project_id"),
    @Index(name = "idx_file_path", columnList = "path")
})
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(nullable = false, length = 500)
    private String path;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 50)
    private String mimeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileType type = FileType.CODE;

    @Column(nullable = false)
    private boolean generated = false;

    @Column(nullable = false)
    private boolean locked = false;

    @Column
    private Long size;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    // No-args constructor
    public FileEntity() {}

    // All-args constructor
    public FileEntity(UUID id, ProjectEntity project, String path, String name, String content,
                      String mimeType, FileType type, boolean generated, boolean locked,
                      Long size, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.project = project;
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

    public ProjectEntity getProject() { return project; }
    public void setProject(ProjectEntity project) { this.project = project; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public FileType getType() { return type; }
    public void setType(FileType type) { this.type = type; }

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

    // Builder pattern
    public static FileEntityBuilder builder() { return new FileEntityBuilder(); }

    public static class FileEntityBuilder {
        private UUID id;
        private ProjectEntity project;
        private String path;
        private String name;
        private String content;
        private String mimeType;
        private FileType type = FileType.CODE;
        private boolean generated = false;
        private boolean locked = false;
        private Long size;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();

        public FileEntityBuilder id(UUID id) { this.id = id; return this; }
        public FileEntityBuilder project(ProjectEntity project) { this.project = project; return this; }
        public FileEntityBuilder path(String path) { this.path = path; return this; }
        public FileEntityBuilder name(String name) { this.name = name; return this; }
        public FileEntityBuilder content(String content) { this.content = content; return this; }
        public FileEntityBuilder mimeType(String mimeType) { this.mimeType = mimeType; return this; }
        public FileEntityBuilder type(FileType type) { this.type = type; return this; }
        public FileEntityBuilder generated(boolean generated) { this.generated = generated; return this; }
        public FileEntityBuilder locked(boolean locked) { this.locked = locked; return this; }
        public FileEntityBuilder size(Long size) { this.size = size; return this; }
        public FileEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public FileEntityBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public FileEntity build() {
            return new FileEntity(id, project, path, name, content, mimeType, type,
                    generated, locked, size, createdAt, updatedAt);
        }
    }

    // equals and hashCode based on id only
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileEntity that = (FileEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "FileEntity{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", name='" + name + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", type=" + type +
                ", generated=" + generated +
                ", locked=" + locked +
                ", size=" + size +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    @PrePersist
    protected void onPersist() {
        if (content != null) {
            size = (long) content.length();
        }
        if (name == null && path != null) {
            int lastSlash = path.lastIndexOf('/');
            name = lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
        }
        if (mimeType == null) {
            mimeType = detectMimeType(path);
        }
    }

    /**
     * Detect MIME type from file extension
     */
    private String detectMimeType(String path) {
        if (path == null) return "text/plain";
        
        String lower = path.toLowerCase();
        if (lower.endsWith(".ts") || lower.endsWith(".tsx")) return "text/typescript";
        if (lower.endsWith(".js") || lower.endsWith(".jsx")) return "text/javascript";
        if (lower.endsWith(".json")) return "application/json";
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".html")) return "text/html";
        if (lower.endsWith(".md")) return "text/markdown";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
        
        return "text/plain";
    }

    /**
     * File type enum
     */
    public enum FileType {
        CODE,
        CONFIG,
        ASSET,
        DOCUMENTATION,
        TEMPLATE
    }
}
