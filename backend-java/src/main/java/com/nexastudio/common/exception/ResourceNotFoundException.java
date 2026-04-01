package com.nexastudio.common.exception;

import com.nexastudio.common.Constants;

/**
 * Exception thrown when a requested resource is not found.
 */
public class ResourceNotFoundException extends NexaStudioException {
    
    public ResourceNotFoundException(String resource, Object id) {
        super(Constants.ERR_NOT_FOUND, 
              String.format("%s not found with id: %s", resource, id));
    }
    
    public ResourceNotFoundException(String message) {
        super(Constants.ERR_NOT_FOUND, message);
    }
}
