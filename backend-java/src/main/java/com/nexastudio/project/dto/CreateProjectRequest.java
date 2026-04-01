package com.nexastudio.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new project.
 */
public class CreateProjectRequest {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;
    
    private String type; // NEXTJS, REACT, VUE, ANGULAR
    
    private String initialPrompt; // Optional initial prompt to generate

    public CreateProjectRequest() {}

    public CreateProjectRequest(String name, String description, String type, String initialPrompt) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.initialPrompt = initialPrompt;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getInitialPrompt() { return initialPrompt; }
    public void setInitialPrompt(String initialPrompt) { this.initialPrompt = initialPrompt; }

    // Builder
    public static CreateProjectRequestBuilder builder() { return new CreateProjectRequestBuilder(); }

    public static class CreateProjectRequestBuilder {
        private String name;
        private String description;
        private String type;
        private String initialPrompt;

        public CreateProjectRequestBuilder name(String name) { this.name = name; return this; }
        public CreateProjectRequestBuilder description(String description) { this.description = description; return this; }
        public CreateProjectRequestBuilder type(String type) { this.type = type; return this; }
        public CreateProjectRequestBuilder initialPrompt(String initialPrompt) { this.initialPrompt = initialPrompt; return this; }

        public CreateProjectRequest build() { return new CreateProjectRequest(name, description, type, initialPrompt); }
    }
}
