package com.nexastudio.project;

import com.nexastudio.user.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Project entity representing a NexaStudio project.
 * Contains metadata and relationship to files and prompts.
 */
@Entity
@Table(name = "projects", indexes = {
    @Index(name = "idx_project_user", columnList = "user_id"),
    @Index(name = "idx_project_created", columnList = "created_at")
})
public class ProjectEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 1000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type = ProjectType.NEXTJS;

    @Column(length = 500)
    private String previewUrl;

    @Column
    private Integer previewPort;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> files = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    private List<PromptEntity> prompts = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant lastGeneratedAt;

    // No-args constructor
    public ProjectEntity() {}

    // All-args constructor
    public ProjectEntity(UUID id, String name, String description, UserEntity user,
                         ProjectStatus status, ProjectType type, String previewUrl,
                         Integer previewPort, List<FileEntity> files, List<PromptEntity> prompts,
                         Instant createdAt, Instant updatedAt, Instant lastGeneratedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.user = user;
        this.status = status;
        this.type = type;
        this.previewUrl = previewUrl;
        this.previewPort = previewPort;
        this.files = files != null ? files : new ArrayList<>();
        this.prompts = prompts != null ? prompts : new ArrayList<>();
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

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }

    public ProjectType getType() { return type; }
    public void setType(ProjectType type) { this.type = type; }

    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String previewUrl) { this.previewUrl = previewUrl; }

    public Integer getPreviewPort() { return previewPort; }
    public void setPreviewPort(Integer previewPort) { this.previewPort = previewPort; }

    public List<FileEntity> getFiles() { return files; }
    public void setFiles(List<FileEntity> files) { this.files = files; }

    public List<PromptEntity> getPrompts() { return prompts; }
    public void setPrompts(List<PromptEntity> prompts) { this.prompts = prompts; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getLastGeneratedAt() { return lastGeneratedAt; }
    public void setLastGeneratedAt(Instant lastGeneratedAt) { this.lastGeneratedAt = lastGeneratedAt; }

    // Builder pattern
    public static ProjectEntityBuilder builder() { return new ProjectEntityBuilder(); }

    public static class ProjectEntityBuilder {
        private UUID id;
        private String name;
        private String description;
        private UserEntity user;
        private ProjectStatus status = ProjectStatus.ACTIVE;
        private ProjectType type = ProjectType.NEXTJS;
        private String previewUrl;
        private Integer previewPort;
        private List<FileEntity> files = new ArrayList<>();
        private List<PromptEntity> prompts = new ArrayList<>();
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();
        private Instant lastGeneratedAt;

        public ProjectEntityBuilder id(UUID id) { this.id = id; return this; }
        public ProjectEntityBuilder name(String name) { this.name = name; return this; }
        public ProjectEntityBuilder description(String description) { this.description = description; return this; }
        public ProjectEntityBuilder user(UserEntity user) { this.user = user; return this; }
        public ProjectEntityBuilder status(ProjectStatus status) { this.status = status; return this; }
        public ProjectEntityBuilder type(ProjectType type) { this.type = type; return this; }
        public ProjectEntityBuilder previewUrl(String previewUrl) { this.previewUrl = previewUrl; return this; }
        public ProjectEntityBuilder previewPort(Integer previewPort) { this.previewPort = previewPort; return this; }
        public ProjectEntityBuilder files(List<FileEntity> files) { this.files = files; return this; }
        public ProjectEntityBuilder prompts(List<PromptEntity> prompts) { this.prompts = prompts; return this; }
        public ProjectEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public ProjectEntityBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public ProjectEntityBuilder lastGeneratedAt(Instant lastGeneratedAt) { this.lastGeneratedAt = lastGeneratedAt; return this; }

        public ProjectEntity build() {
            return new ProjectEntity(id, name, description, user, status, type, previewUrl,
                    previewPort, files, prompts, createdAt, updatedAt, lastGeneratedAt);
        }
    }

    // equals and hashCode based on id only
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProjectEntity that = (ProjectEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "ProjectEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", type=" + type +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * Add a file to the project
     */
    public void addFile(FileEntity file) {
        files.add(file);
        file.setProject(this);
    }

    /**
     * Remove a file from the project
     */
    public void removeFile(FileEntity file) {
        files.remove(file);
        file.setProject(null);
    }

    /**
     * Add a prompt to the project
     */
    public void addPrompt(PromptEntity prompt) {
        prompts.add(prompt);
        prompt.setProject(this);
    }

    /**
     * Project status enum
     */
    public enum ProjectStatus {
        ACTIVE,
        ARCHIVED,
        DELETED
    }

    /**
     * Project type enum
     */
    public enum ProjectType {
        NEXTJS,
        REACT,
        VUE,
        ANGULAR,
        SVELTE,
        ASTRO,
        SOLID,
        REMIX,
        VANILLA
    }
}
