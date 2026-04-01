package com.nexastudio.common.exception;

import com.nexastudio.common.Constants;

/**
 * Exception thrown when validation fails.
 */
public class ValidationException extends NexaStudioException {
    
    public ValidationException(String message) {
        super(Constants.ERR_VALIDATION, message);
    }
    
    public ValidationException(String message, Object details) {
        super(Constants.ERR_VALIDATION, message, details);
    }
}
