package com.nexastudio.project;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Prompt entity representing a user prompt in a project.
 * Tracks all AI generation requests and their results.
 */
@Entity
@Table(name = "prompts", indexes = {
    @Index(name = "idx_prompt_project", columnList = "project_id"),
    @Index(name = "idx_prompt_created", columnList = "created_at")
})
public class PromptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromptIntent intent = PromptIntent.GENERATE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PromptStatus status = PromptStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String aiResponse;

    @Column
    private Integer tokensUsed;

    @Column
    private Long processingTimeMs;

    @Column(length = 1000)
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant completedAt;

    // No-args constructor
    public PromptEntity() {}

    // All-args constructor
    public PromptEntity(UUID id, ProjectEntity project, String text, PromptIntent intent,
                        PromptStatus status, String aiResponse, Integer tokensUsed,
                        Long processingTimeMs, String errorMessage, Instant createdAt,
                        Instant completedAt) {
        this.id = id;
        this.project = project;
        this.text = text;
        this.intent = intent;
        this.status = status;
        this.aiResponse = aiResponse;
        this.tokensUsed = tokensUsed;
        this.processingTimeMs = processingTimeMs;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ProjectEntity getProject() { return project; }
    public void setProject(ProjectEntity project) { this.project = project; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public PromptIntent getIntent() { return intent; }
    public void setIntent(PromptIntent intent) { this.intent = intent; }

    public PromptStatus getStatus() { return status; }
    public void setStatus(PromptStatus status) { this.status = status; }

    public String getAiResponse() { return aiResponse; }
    public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }

    public Integer getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; }

    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    // Builder pattern
    public static PromptEntityBuilder builder() { return new PromptEntityBuilder(); }

    public static class PromptEntityBuilder {
        private UUID id;
        private ProjectEntity project;
        private String text;
        private PromptIntent intent = PromptIntent.GENERATE;
        private PromptStatus status = PromptStatus.PENDING;
        private String aiResponse;
        private Integer tokensUsed;
        private Long processingTimeMs;
        private String errorMessage;
        private Instant createdAt = Instant.now();
        private Instant completedAt;

        public PromptEntityBuilder id(UUID id) { this.id = id; return this; }
        public PromptEntityBuilder project(ProjectEntity project) { this.project = project; return this; }
        public PromptEntityBuilder text(String text) { this.text = text; return this; }
        public PromptEntityBuilder intent(PromptIntent intent) { this.intent = intent; return this; }
        public PromptEntityBuilder status(PromptStatus status) { this.status = status; return this; }
        public PromptEntityBuilder aiResponse(String aiResponse) { this.aiResponse = aiResponse; return this; }
        public PromptEntityBuilder tokensUsed(Integer tokensUsed) { this.tokensUsed = tokensUsed; return this; }
        public PromptEntityBuilder processingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; return this; }
        public PromptEntityBuilder errorMessage(String errorMessage) { this.errorMessage = errorMessage; return this; }
        public PromptEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public PromptEntityBuilder completedAt(Instant completedAt) { this.completedAt = completedAt; return this; }

        public PromptEntity build() {
            return new PromptEntity(id, project, text, intent, status, aiResponse,
                    tokensUsed, processingTimeMs, errorMessage, createdAt, completedAt);
        }
    }

    // equals and hashCode based on id only
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptEntity that = (PromptEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PromptEntity{" +
                "id=" + id +
                ", intent=" + intent +
                ", status=" + status +
                ", tokensUsed=" + tokensUsed +
                ", processingTimeMs=" + processingTimeMs +
                ", createdAt=" + createdAt +
                '}';
    }

    /**
     * Prompt intent enum - what the user wants to do
     */
    public enum PromptIntent {
        GENERATE,      // Generate new code
        MODIFY,        // Modify existing code
        FIX,           // Fix a bug
        EXPLAIN,       // Explain code
        REFACTOR,      // Refactor code
        ADD_FEATURE,   // Add a feature
        STYLE          // Style/UI changes
    }

    /**
     * Prompt status enum
     */
    public enum PromptStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}
