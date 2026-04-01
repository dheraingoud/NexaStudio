package com.nexastudio.user;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexastudio.common.Constants;
import com.nexastudio.common.exception.ResourceNotFoundException;

/**
 * Service for User operations.
 * Also implements UserDetailsService for Spring Security.
 */
@Service
public class UserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Load user by username for Spring Security
     */
    @Override
    @Cacheable(value = "users", key = "#username", unless = "#result == null")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);
        return userRepository.findActiveUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id.toString()", unless = "#result == null")
    public UserEntity findById(UUID id) {
        log.debug("Finding user by ID: {}", id);
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    /**
     * Find user by username
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#username", unless = "#result == null")
    public UserEntity findByUsername(String username) {
        log.debug("Finding user by username: {}", username);
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#email", unless = "#result == null")
    public UserEntity findByEmail(String email) {
        log.debug("Finding user by email: {}", email);
        return userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    /**
     * Check if username is already registered
     */
    @Transactional(readOnly = true)
    public boolean usernameExists(String username) {
        return userRepository.existsByUsernameIgnoreCase(username);
    }

    /**
     * Check if email is already registered
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    /**
     * Save user
     */
    @Transactional
    @CacheEvict(value = "users", allEntries = true)
    public UserEntity save(UserEntity user) {
        log.debug("Saving user: {}", user.getUsername());
        return userRepository.save(user);
    }

    /**
     * Update last login timestamp
     */
    @Transactional
    @CacheEvict(value = "users", key = "#userId.toString()")
    public void updateLastLogin(UUID userId) {
        userRepository.updateLastLogin(userId, Instant.now());
    }

    /**
     * Get user's project limit based on plan
     */
    public int getProjectLimit(UserEntity user) {
        return switch (user.getPlan()) {
            case FREE -> Constants.MAX_PROJECTS_FREE;
            case PRO -> Constants.MAX_PROJECTS_PREMIUM;
            case ENTERPRISE -> 500;
        };
    }

    /**
     * Get user's AI request limit per hour
     */
    public int getAiRequestLimit(UserEntity user) {
        return switch (user.getPlan()) {
            case FREE -> 20;
            case PRO -> 100;
            case ENTERPRISE -> 1000;
        };
    }
}
