package com.nexastudio.common;

/**
 * Application-wide constants for NexaStudio.
 * Centralized location for all constant values used across the application.
 */
public final class Constants {
    
    private Constants() {
        throw new UnsupportedOperationException("Cannot instantiate Constants class");
    }
    
    // API Versioning
    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
    
    // Authentication
    public static final String AUTH_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_TYPE = "JWT";
    
    // User Roles
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_PREMIUM = "ROLE_PREMIUM";
    
    // Project Constants
    public static final int MAX_PROJECTS_FREE = 20;
    public static final int MAX_PROJECTS_PREMIUM = 100;
    public static final int MAX_FILES_PER_PROJECT = 500;
    public static final long MAX_FILE_SIZE_BYTES = 1024 * 1024; // 1MB
    
    // AI Constants
    public static final int MAX_PROMPT_LENGTH = 10000;
    public static final int MAX_CONTEXT_TOKENS = 32000;
    public static final int MAX_RETRIES = 3;
    public static final int RETRY_DELAY_MS = 1000;
    
    // Next.js Template Paths
    public static final String[] NEXTJS_TEMPLATE_FOLDERS = {
        "app",
        "components", 
        "lib",
        "public",
        "styles"
    };
    
    // Allowed File Extensions
    public static final String[] ALLOWED_EXTENSIONS = {
        ".ts", ".tsx", ".js", ".jsx",
        ".json", ".css", ".scss",
        ".html", ".md", ".txt",
        ".svg", ".png", ".jpg", ".ico"
    };
    
    // Protected Files (AI cannot modify)
    public static final String[] PROTECTED_FILES = {
        "package-lock.json",
        "node_modules",
        ".git",
        ".env.local"
    };
    
    // Preview Constants
    public static final int PREVIEW_PORT_START = 3100;
    public static final int PREVIEW_PORT_END = 3200;
    public static final long PREVIEW_TIMEOUT_MS = 300000; // 5 minutes
    
    // Rate Limiting
    public static final int RATE_LIMIT_REQUESTS_PER_MINUTE = 60;
    public static final int RATE_LIMIT_AI_REQUESTS_PER_HOUR = 100;
    
    // Error Codes
    public static final String ERR_UNAUTHORIZED = "UNAUTHORIZED";
    public static final String ERR_FORBIDDEN = "FORBIDDEN";
    public static final String ERR_NOT_FOUND = "NOT_FOUND";
    public static final String ERR_VALIDATION = "VALIDATION_ERROR";
    public static final String ERR_AI_GENERATION = "AI_GENERATION_ERROR";
    public static final String ERR_RATE_LIMITED = "RATE_LIMITED";
    public static final String ERR_INTERNAL = "INTERNAL_ERROR";
    public static final String ERR_PROJECT_LIMIT = "PROJECT_LIMIT_EXCEEDED";
    public static final String ERR_FILE_TOO_LARGE = "FILE_TOO_LARGE";
    public static final String ERR_INVALID_FILE_TYPE = "INVALID_FILE_TYPE";
}
