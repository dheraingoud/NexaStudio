package com.nexastudio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Rate Limiting Configuration Properties.
 */
@Configuration
@ConfigurationProperties(prefix = "rate-limit")
public class RateLimitConfig {

    private int requestsPerMinute = 60;
    private int aiRequestsPerHour = 100;
    private boolean enabled = true;

    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }

    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }

    public int getAiRequestsPerHour() {
        return aiRequestsPerHour;
    }

    public void setAiRequestsPerHour(int aiRequestsPerHour) {
        this.aiRequestsPerHour = aiRequestsPerHour;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
