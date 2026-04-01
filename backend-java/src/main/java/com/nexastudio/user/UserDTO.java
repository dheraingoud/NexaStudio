package com.nexastudio.user;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for user data transfer.
 * Used to return user information without sensitive data.
 */
public class UserDTO {
    
    private UUID id;
    private String username;
    private String email;
    private String avatarUrl;
    private String role;
    private String plan;
    private boolean emailVerified;
    private Instant createdAt;
    private Instant lastLoginAt;

    public UserDTO() {}

    public UserDTO(UUID id, String username, String email, String avatarUrl, String role,
                   String plan, boolean emailVerified, Instant createdAt, Instant lastLoginAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.plan = plan;
        this.emailVerified = emailVerified;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    // Builder
    public static UserDTOBuilder builder() { return new UserDTOBuilder(); }

    public static class UserDTOBuilder {
        private UUID id;
        private String username;
        private String email;
        private String avatarUrl;
        private String role;
        private String plan;
        private boolean emailVerified;
        private Instant createdAt;
        private Instant lastLoginAt;

        public UserDTOBuilder id(UUID id) { this.id = id; return this; }
        public UserDTOBuilder username(String username) { this.username = username; return this; }
        public UserDTOBuilder email(String email) { this.email = email; return this; }
        public UserDTOBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public UserDTOBuilder role(String role) { this.role = role; return this; }
        public UserDTOBuilder plan(String plan) { this.plan = plan; return this; }
        public UserDTOBuilder emailVerified(boolean emailVerified) { this.emailVerified = emailVerified; return this; }
        public UserDTOBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public UserDTOBuilder lastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }

        public UserDTO build() {
            return new UserDTO(id, username, email, avatarUrl, role, plan, emailVerified, createdAt, lastLoginAt);
        }
    }
    
    /**
     * Convert entity to DTO
     */
    public static UserDTO fromEntity(UserEntity entity) {
        return UserDTO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .avatarUrl(entity.getAvatarUrl())
                .role(entity.getRole().name())
                .plan(entity.getPlan().name())
                .emailVerified(entity.isEmailVerified())
                .createdAt(entity.getCreatedAt())
                .lastLoginAt(entity.getLastLoginAt())
                .build();
    }
}
