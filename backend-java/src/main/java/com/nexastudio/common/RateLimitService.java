package com.nexastudio.common;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.nexastudio.config.RateLimitConfig;

/**
 * In-memory Rate Limiting Service.
 * Sliding window rate limiting for API endpoints and AI requests.
 */
@Service
public class RateLimitService {

    private final Map<String, LocalCounter> localCounters = new ConcurrentHashMap<>();
    private final RateLimitConfig config;

    public RateLimitService(RateLimitConfig config) {
        this.config = config;
    }

    public RateLimitResult checkRateLimit(String identifier) {
        if (!config.isEnabled()) {
            return RateLimitResult.allowed(config.getRequestsPerMinute(), config.getRequestsPerMinute());
        }
        return checkLimit("rate:" + identifier, config.getRequestsPerMinute(), Duration.ofMinutes(1));
    }

    public RateLimitResult checkAiRateLimit(UUID userId) {
        if (!config.isEnabled()) {
            return RateLimitResult.allowed(config.getAiRequestsPerHour(), config.getAiRequestsPerHour());
        }
        return checkLimit("ai:" + userId, config.getAiRequestsPerHour(), Duration.ofHours(1));
    }

    public RateLimitResult checkAiRateLimit(String identifier) {
        if (!config.isEnabled()) {
            return RateLimitResult.allowed(config.getAiRequestsPerHour(), config.getAiRequestsPerHour());
        }
        return checkLimit("ai:" + identifier, config.getAiRequestsPerHour(), Duration.ofHours(1));
    }

    private RateLimitResult checkLimit(String key, int maxRequests, Duration window) {
        long now = System.currentTimeMillis();
        long windowMillis = window.toMillis();

        LocalCounter counter = localCounters.compute(key, (k, existing) -> {
            if (existing == null || now - existing.windowStart >= windowMillis) {
                return new LocalCounter(now, 1);
            }
            existing.count++;
            return existing;
        });

        int remaining = Math.max(0, maxRequests - (int) counter.count);
        if (counter.count > maxRequests) {
            long elapsed = now - counter.windowStart;
            long retryAfter = Math.max(1, (windowMillis - elapsed) / 1000);
            return RateLimitResult.exceeded(maxRequests, 0, retryAfter);
        }
        return RateLimitResult.allowed(maxRequests, remaining);
    }

    public int getRemainingRequests(String identifier) {
        return getRemaining("rate:" + identifier, config.getRequestsPerMinute(), Duration.ofMinutes(1));
    }

    public int getRemainingAiRequests(UUID userId) {
        return getRemaining("ai:" + userId, config.getAiRequestsPerHour(), Duration.ofHours(1));
    }

    private int getRemaining(String key, int max, Duration window) {
        LocalCounter counter = localCounters.get(key);
        if (counter == null) return max;
        long now = System.currentTimeMillis();
        if (now - counter.windowStart >= window.toMillis()) {
            localCounters.remove(key);
            return max;
        }
        return Math.max(0, max - (int) counter.count);
    }

    public void resetRateLimit(String identifier) {
        localCounters.remove("rate:" + identifier);
    }

    private static final class LocalCounter {
        long windowStart;
        long count;
        LocalCounter(long windowStart, long count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }

    public static class RateLimitResult {
        private final boolean allowed;
        private final int limit;
        private final int remaining;
        private final long retryAfterSeconds;

        private RateLimitResult(boolean allowed, int limit, int remaining, long retryAfterSeconds) {
            this.allowed = allowed;
            this.limit = limit;
            this.remaining = remaining;
            this.retryAfterSeconds = retryAfterSeconds;
        }

        public static RateLimitResult allowed(int limit, int remaining) {
            return new RateLimitResult(true, limit, remaining, 0);
        }

        public static RateLimitResult exceeded(int limit, int remaining, long retryAfterSeconds) {
            return new RateLimitResult(false, limit, remaining, retryAfterSeconds);
        }

        public boolean isAllowed() { return allowed; }
        public int getLimit() { return limit; }
        public int getRemaining() { return remaining; }
        public long getRetryAfterSeconds() { return retryAfterSeconds; }
    }
}
