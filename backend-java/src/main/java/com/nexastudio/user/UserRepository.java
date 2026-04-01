package com.nexastudio.user;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for User entity operations.
 */
@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Find user by username (case-insensitive)
     */
    Optional<UserEntity> findByUsernameIgnoreCase(String username);

    /**
     * Check if username exists
     */
    boolean existsByUsernameIgnoreCase(String username);

    /**
     * Find user by email (case-insensitive)
     */
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    /**
     * Check if email exists
     */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Update last login timestamp
     */
    @Modifying
    @Query("UPDATE UserEntity u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLogin(@Param("userId") UUID userId, @Param("loginTime") Instant loginTime);

    /**
     * Find by username for authentication
     */
    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.username) = LOWER(:username) AND u.active = true")
    Optional<UserEntity> findActiveUserByUsername(@Param("username") String username);
}
