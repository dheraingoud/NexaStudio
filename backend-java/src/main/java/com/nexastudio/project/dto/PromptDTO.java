package com.nexastudio.project.dto;

import com.nexastudio.project.PromptEntity;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for prompt / chat history entries.
 */
public class PromptDTO {

    private UUID id;
    private String text;
    private String intent;
    private String status;
    private String aiResponse;
    private Long processingTimeMs;
    private String errorMessage;
    private Instant createdAt;
    private Instant completedAt;

    public PromptDTO() {}

    public PromptDTO(UUID id, String text, String intent, String status, String aiResponse,
                     Long processingTimeMs, String errorMessage, Instant createdAt, Instant completedAt) {
        this.id = id;
        this.text = text;
        this.intent = intent;
        this.status = status;
        this.aiResponse = aiResponse;
        this.processingTimeMs = processingTimeMs;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.completedAt = completedAt;
    }

    public static PromptDTO fromEntity(PromptEntity entity) {
        return new PromptDTO(
                entity.getId(),
                entity.getText(),
                entity.getIntent().name(),
                entity.getStatus().name(),
                entity.getAiResponse(),
                entity.getProcessingTimeMs(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getCompletedAt()
        );
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAiResponse() { return aiResponse; }
    public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }

    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}
