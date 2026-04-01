package com.nexastudio.common.exception;

import com.nexastudio.common.ApiResponse;
import com.nexastudio.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the entire application.
 * Provides consistent error responses across all endpoints.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle NexaStudio custom exceptions
     */
    @ExceptionHandler(NexaStudioException.class)
    public ResponseEntity<ApiResponse<Object>> handleNexaStudioException(
            NexaStudioException ex, WebRequest request) {
        
        log.error("NexaStudio exception: {} - {}", ex.getErrorCode(), ex.getMessage());
        
        HttpStatus status = mapErrorCodeToStatus(ex.getErrorCode());
        ApiResponse<Object> response = ApiResponse.error(
                ex.getErrorCode(), 
                ex.getMessage(), 
                ex.getDetails()
        );
        
        return new ResponseEntity<>(response, status);
    }

    /**
     * Handle validation exceptions from @Valid annotations
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.warn("Validation failed: {}", errors);
        
        ApiResponse<Object> response = ApiResponse.error(
                Constants.ERR_VALIDATION,
                "Validation failed",
                errors
        );
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handle authentication exceptions
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(
            AuthenticationException ex) {
        
        log.warn("Authentication failed: {}", ex.getMessage());
        
        String message = "Authentication failed";
        if (ex instanceof BadCredentialsException) {
            message = "Invalid username or password";
        }
        
        ApiResponse<Object> response = ApiResponse.error(
                Constants.ERR_UNAUTHORIZED,
                message
        );
        
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Handle access denied exceptions
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
            AccessDeniedException ex) {
        
        log.warn("Access denied: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                Constants.ERR_FORBIDDEN,
                "You don't have permission to access this resource"
        );
        
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    /**
     * Handle rate limit exceptions
     */
    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<ApiResponse<Object>> handleRateLimitException(
            RateLimitException ex) {
        
        log.warn("Rate limit exceeded: {}", ex.getMessage());
        
        ApiResponse<Object> response = ApiResponse.error(
                Constants.ERR_RATE_LIMITED,
                ex.getMessage(),
                ex.getDetails()
        );
        
        return new ResponseEntity<>(response, HttpStatus.TOO_MANY_REQUESTS);
    }

    /**
     * Handle all other unexpected exceptions
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        log.error("Unexpected error occurred", ex);
        
        ApiResponse<Object> response = ApiResponse.error(
                Constants.ERR_INTERNAL,
                "An unexpected error occurred. Please try again later."
        );
        
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Map error codes to HTTP status codes
     */
    private HttpStatus mapErrorCodeToStatus(String errorCode) {
        return switch (errorCode) {
            case Constants.ERR_UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case Constants.ERR_FORBIDDEN -> HttpStatus.FORBIDDEN;
            case Constants.ERR_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case Constants.ERR_VALIDATION, Constants.ERR_FILE_TOO_LARGE, 
                 Constants.ERR_INVALID_FILE_TYPE, Constants.ERR_PROJECT_LIMIT -> HttpStatus.BAD_REQUEST;
            case Constants.ERR_RATE_LIMITED -> HttpStatus.TOO_MANY_REQUESTS;
            case Constants.ERR_AI_GENERATION -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
