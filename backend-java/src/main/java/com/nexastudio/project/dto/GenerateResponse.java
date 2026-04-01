package com.nexastudio.project.dto;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for AI generation results.
 */
public class GenerateResponse {
    
    private UUID promptId;
    private String status;
    private List<FileChange> changes;
    private String explanation;
    private int tokensUsed;
    private long processingTimeMs;

    public GenerateResponse() {}

    public GenerateResponse(UUID promptId, String status, List<FileChange> changes,
                            String explanation, int tokensUsed, long processingTimeMs) {
        this.promptId = promptId;
        this.status = status;
        this.changes = changes;
        this.explanation = explanation;
        this.tokensUsed = tokensUsed;
        this.processingTimeMs = processingTimeMs;
    }

    // Getters and Setters
    public UUID getPromptId() { return promptId; }
    public void setPromptId(UUID promptId) { this.promptId = promptId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<FileChange> getChanges() { return changes; }
    public void setChanges(List<FileChange> changes) { this.changes = changes; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public int getTokensUsed() { return tokensUsed; }
    public void setTokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; }

    public long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    // Builder
    public static GenerateResponseBuilder builder() { return new GenerateResponseBuilder(); }

    public static class GenerateResponseBuilder {
        private UUID promptId;
        private String status;
        private List<FileChange> changes;
        private String explanation;
        private int tokensUsed;
        private long processingTimeMs;

        public GenerateResponseBuilder promptId(UUID promptId) { this.promptId = promptId; return this; }
        public GenerateResponseBuilder status(String status) { this.status = status; return this; }
        public GenerateResponseBuilder changes(List<FileChange> changes) { this.changes = changes; return this; }
        public GenerateResponseBuilder explanation(String explanation) { this.explanation = explanation; return this; }
        public GenerateResponseBuilder tokensUsed(int tokensUsed) { this.tokensUsed = tokensUsed; return this; }
        public GenerateResponseBuilder processingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; return this; }

        public GenerateResponse build() {
            return new GenerateResponse(promptId, status, changes, explanation, tokensUsed, processingTimeMs);
        }
    }
    
    public static class FileChange {
        private String path;
        private String action; // CREATE, UPDATE, DELETE
        private String content;
        private String summary;

        public FileChange() {}

        public FileChange(String path, String action, String content, String summary) {
            this.path = path;
            this.action = action;
            this.content = content;
            this.summary = summary;
        }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }

        public String getAction() { return action; }
        public void setAction(String action) { this.action = action; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }

        public static FileChangeBuilder builder() { return new FileChangeBuilder(); }

        public static class FileChangeBuilder {
            private String path;
            private String action;
            private String content;
            private String summary;

            public FileChangeBuilder path(String path) { this.path = path; return this; }
            public FileChangeBuilder action(String action) { this.action = action; return this; }
            public FileChangeBuilder content(String content) { this.content = content; return this; }
            public FileChangeBuilder summary(String summary) { this.summary = summary; return this; }

            public FileChange build() { return new FileChange(path, action, content, summary); }
        }
    }
}
