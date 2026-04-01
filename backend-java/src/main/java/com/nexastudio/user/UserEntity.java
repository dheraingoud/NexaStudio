package com.nexastudio.user;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/**
 * User entity representing a NexaStudio user.
 * Implements UserDetails for Spring Security integration.
 */
@Entity
@Table(name = "users")
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 255)
    private String email;

    @Column(length = 500)
    private String avatarUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserPlan plan = UserPlan.FREE;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant lastLoginAt;

    // Default constructor
    public UserEntity() {}

    // All-args constructor
    public UserEntity(UUID id, String username, String passwordHash, String email, String avatarUrl,
                      UserRole role, UserPlan plan, boolean emailVerified, boolean active,
                      Instant createdAt, Instant updatedAt, Instant lastLoginAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.role = role;
        this.plan = plan;
        this.emailVerified = emailVerified;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.lastLoginAt = lastLoginAt;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public String getUsernameField() { return username; }
    public void setUsernameField(String username) { this.username = username; }
    
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
    
    public UserPlan getPlan() { return plan; }
    public void setPlan(UserPlan plan) { this.plan = plan; }
    
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    public Instant getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; }

    // Builder pattern
    public static UserEntityBuilder builder() { return new UserEntityBuilder(); }

    public static class UserEntityBuilder {
        private UUID id;
        private String username;
        private String passwordHash;
        private String email;
        private String avatarUrl;
        private UserRole role = UserRole.USER;
        private UserPlan plan = UserPlan.FREE;
        private boolean emailVerified = false;
        private boolean active = true;
        private Instant createdAt = Instant.now();
        private Instant updatedAt = Instant.now();
        private Instant lastLoginAt;

        public UserEntityBuilder id(UUID id) { this.id = id; return this; }
        public UserEntityBuilder username(String username) { this.username = username; return this; }
        public UserEntityBuilder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public UserEntityBuilder email(String email) { this.email = email; return this; }
        public UserEntityBuilder avatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; return this; }
        public UserEntityBuilder role(UserRole role) { this.role = role; return this; }
        public UserEntityBuilder plan(UserPlan plan) { this.plan = plan; return this; }
        public UserEntityBuilder emailVerified(boolean emailVerified) { this.emailVerified = emailVerified; return this; }
        public UserEntityBuilder active(boolean active) { this.active = active; return this; }
        public UserEntityBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public UserEntityBuilder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }
        public UserEntityBuilder lastLoginAt(Instant lastLoginAt) { this.lastLoginAt = lastLoginAt; return this; }

        public UserEntity build() {
            return new UserEntity(id, username, passwordHash, email, avatarUrl, role, plan,
                    emailVerified, active, createdAt, updatedAt, lastLoginAt);
        }
    }

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    /**
     * User roles enum
     */
    public enum UserRole {
        USER,
        PREMIUM,
        ADMIN
    }

    /**
     * User plan enum
     */
    public enum UserPlan {
        FREE,
        PRO,
        ENTERPRISE
    }
}
