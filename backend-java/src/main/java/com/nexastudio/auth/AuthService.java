package com.nexastudio.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nexastudio.auth.dto.AuthResponse;
import com.nexastudio.auth.dto.LoginRequest;
import com.nexastudio.auth.dto.RefreshTokenRequest;
import com.nexastudio.auth.dto.RegisterRequest;
import com.nexastudio.common.Constants;
import com.nexastudio.common.exception.ValidationException;
import com.nexastudio.user.UserDTO;
import com.nexastudio.user.UserEntity;
import com.nexastudio.user.UserService;

/**
 * Authentication service handling login, registration, and token management.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthService(UserService userService, JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedUsername = request.getUsername().trim().toLowerCase();
        log.info("Registering new user: {}", normalizedUsername);
        
        // Check if username already exists
        if (userService.usernameExists(normalizedUsername)) {
            throw new ValidationException("Username is already taken");
        }

        // Create new user
        UserEntity user = UserEntity.builder()
                .username(normalizedUsername)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserEntity.UserRole.USER)
                .plan(UserEntity.UserPlan.FREE)
                .build();

        user = userService.save(user);
        log.info("User registered successfully: {}", user.getId());

        // Generate tokens
        return createAuthResponse(user);
    }

    /**
     * Authenticate user and return tokens
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String normalizedUsername = request.getUsername().trim().toLowerCase();
        log.info("Login attempt for: {}", normalizedUsername);
        
        try {
            // Authenticate with Spring Security
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedUsername,
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            log.warn("Login failed for: {}", normalizedUsername);
            throw new BadCredentialsException("Invalid username or password");
        }

        // Get user and update last login
        UserEntity user = userService.findByUsername(normalizedUsername);
        userService.updateLastLogin(user.getId());
        
        log.info("User logged in successfully: {}", user.getId());
        return createAuthResponse(user);
    }

    /**
     * Refresh access token using refresh token
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Validate refresh token
        if (!jwtUtil.isTokenValid(refreshToken)) {
            throw new ValidationException("Invalid or expired refresh token");
        }
        
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new ValidationException("Token is not a refresh token");
        }

        // Extract user info and generate new tokens
        String username = jwtUtil.extractUsername(refreshToken);
        UserEntity user = userService.findByUsername(username);
        
        log.info("Token refreshed for user: {}", user.getId());
        return createAuthResponse(user);
    }

    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String username) {
        UserEntity user = userService.findByUsername(username);
        return UserDTO.fromEntity(user);
    }

    /**
     * Update user password
     */
    @Transactional
    public void updatePassword(String username, String currentPassword, String newPassword) {
        UserEntity user = userService.findByUsername(username);
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new ValidationException("Current password is incorrect");
        }
        
        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userService.save(user);
        
        log.info("Password updated for user: {}", user.getId());
    }

    /**
     * Create authentication response with tokens
     */
    private AuthResponse createAuthResponse(UserEntity user) {
        String accessToken = jwtUtil.generateToken(user, user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user, user.getId());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(Constants.TOKEN_TYPE)
                .expiresIn(jwtExpiration / 1000) // Convert to seconds
                .user(UserDTO.fromEntity(user))
                .build();
    }
}
