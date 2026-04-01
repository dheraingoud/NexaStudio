package com.nexastudio.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

/**
 * HTTP Interceptor for Rate Limiting.
 * Applies rate limits to all incoming requests.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    public RateLimitInterceptor(RateLimitService rateLimitService, ObjectMapper objectMapper) {
        this.rateLimitService = rateLimitService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip rate limiting for health checks and actuator
        String path = request.getRequestURI();
        if (path.contains("/health") || path.contains("/actuator")) {
            return true;
        }

        String identifier = getIdentifier(request);
        RateLimitService.RateLimitResult result = rateLimitService.checkRateLimit(identifier);

        // Add rate limit headers
        response.setHeader("X-RateLimit-Limit", String.valueOf(result.getLimit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(result.getRemaining()));

        if (!result.isAllowed()) {
            response.setHeader("X-RateLimit-Reset", String.valueOf(result.getRetryAfterSeconds()));
            response.setHeader("Retry-After", String.valueOf(result.getRetryAfterSeconds()));
            
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ApiResponse<Void> errorResponse = ApiResponse.<Void>builder()
                .success(false)
                .error(new ApiResponse.ErrorDetails(
                    "RATE_LIMIT_EXCEEDED",
                    "Too many requests. Please try again in " + result.getRetryAfterSeconds() + " seconds.",
                    null
                ))
                .timestamp(Instant.now())
                .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            
            log.warn("Rate limit exceeded for identifier: {} on path: {}", identifier, path);
            return false;
        }

        return true;
    }

    /**
     * Get identifier for rate limiting.
     * Uses user ID if authenticated, otherwise uses IP address.
     */
    private String getIdentifier(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }
        
        // Fall back to IP address
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            // X-Forwarded-For can contain multiple IPs, use the first one
            ip = ip.split(",")[0].trim();
        }
        
        return "ip:" + ip;
    }
}
