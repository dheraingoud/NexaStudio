package com.nexastudio.project.dto;

import jakarta.validation.constraints.Size;

/**
 * Request DTO for updating a project.
 */
public class UpdateProjectRequest {
    
    @Size(min = 1, max = 255, message = "Project name must be between 1 and 255 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    public UpdateProjectRequest() {}

    public UpdateProjectRequest(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Builder
    public static UpdateProjectRequestBuilder builder() { return new UpdateProjectRequestBuilder(); }

    public static class UpdateProjectRequestBuilder {
        private String name;
        private String description;

        public UpdateProjectRequestBuilder name(String name) { this.name = name; return this; }
        public UpdateProjectRequestBuilder description(String description) { this.description = description; return this; }

        public UpdateProjectRequest build() { return new UpdateProjectRequest(name, description); }
    }
}
