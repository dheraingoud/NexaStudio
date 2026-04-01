package com.nexastudio.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Token refresh request DTO.
 */
public class RefreshTokenRequest {
    
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public RefreshTokenRequest() {}

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters and Setters
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    // Builder
    public static RefreshTokenRequestBuilder builder() { return new RefreshTokenRequestBuilder(); }

    public static class RefreshTokenRequestBuilder {
        private String refreshToken;

        public RefreshTokenRequestBuilder refreshToken(String refreshToken) { this.refreshToken = refreshToken; return this; }

        public RefreshTokenRequest build() { return new RefreshTokenRequest(refreshToken); }
    }
}
