package com.nexastudio.common.exception;

import com.nexastudio.common.Constants;

/**
 * Exception thrown when rate limit is exceeded.
 */
public class RateLimitException extends NexaStudioException {
    
    public RateLimitException(String message) {
        super(Constants.ERR_RATE_LIMITED, message);
    }
    
    public RateLimitException(String message, long retryAfterSeconds) {
        super(Constants.ERR_RATE_LIMITED, message, retryAfterSeconds);
    }
}
