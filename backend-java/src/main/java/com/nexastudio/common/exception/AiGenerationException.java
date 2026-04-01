package com.nexastudio.common.exception;

import com.nexastudio.common.Constants;

/**
 * Exception thrown when AI generation fails.
 */
public class AiGenerationException extends NexaStudioException {
    
    public AiGenerationException(String message) {
        super(Constants.ERR_AI_GENERATION, message);
    }
    
    public AiGenerationException(String message, Throwable cause) {
        super(Constants.ERR_AI_GENERATION, message, cause);
    }
    
    public AiGenerationException(String message, Object details) {
        super(Constants.ERR_AI_GENERATION, message, details);
    }
}
