package com.nexastudio.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for AI generation prompt.
 */
public class GenerateRequest {
    
    @NotBlank(message = "Prompt is required")
    @Size(min = 1, max = 10000, message = "Prompt must be between 1 and 10000 characters")
    private String prompt;
    
    private String intent; // GENERATE, MODIFY, FIX, EXPLAIN, REFACTOR, ADD_FEATURE, STYLE
    
    private String targetFile; // Optional: specific file to modify

    public GenerateRequest() {}

    public GenerateRequest(String prompt, String intent, String targetFile) {
        this.prompt = prompt;
        this.intent = intent;
        this.targetFile = targetFile;
    }

    // Getters and Setters
    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }

    public String getTargetFile() { return targetFile; }
    public void setTargetFile(String targetFile) { this.targetFile = targetFile; }

    // Builder
    public static GenerateRequestBuilder builder() { return new GenerateRequestBuilder(); }

    public static class GenerateRequestBuilder {
        private String prompt;
        private String intent;
        private String targetFile;

        public GenerateRequestBuilder prompt(String prompt) { this.prompt = prompt; return this; }
        public GenerateRequestBuilder intent(String intent) { this.intent = intent; return this; }
        public GenerateRequestBuilder targetFile(String targetFile) { this.targetFile = targetFile; return this; }

        public GenerateRequest build() { return new GenerateRequest(prompt, intent, targetFile); }
    }
}
