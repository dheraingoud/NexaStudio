package com.nexastudio.common.exception;

/**
 * Base exception for all NexaStudio business exceptions.
 * Provides consistent error handling across the application.
 */
public class NexaStudioException extends RuntimeException {
    
    private final String errorCode;
    private final Object details;
    
    public NexaStudioException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
    }
    
    public NexaStudioException(String errorCode, String message, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public NexaStudioException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = null;
    }

    public String getErrorCode() { return errorCode; }
    public Object getDetails() { return details; }
}
