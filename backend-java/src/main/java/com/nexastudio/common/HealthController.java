package com.nexastudio.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Health Check Controller.
 * Provides health, readiness, and AI model status endpoints.
 */
@RestController
public class HealthController {

    @Value("${gemini.api.key:}")
    private String geminiKey;

    @Value("${nvidia.api.key:}")
    private String nvidiaKey;

    /**
     * Basic health check
     * GET /api/health
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthData = new LinkedHashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "nexastudio-backend");
        healthData.put("timestamp", Instant.now());
        healthData.put("version", "1.0.0");

        // AI model availability
        Map<String, Boolean> models = new LinkedHashMap<>();
        models.put("nvidia", isPresent(nvidiaKey));
        models.put("gemini", isPresent(geminiKey));
        healthData.put("aiModels", models);

        return ResponseEntity.ok(ApiResponse.success(healthData));
    }

    /**
     * Ready check
     * GET /api/ready
     */
    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ready() {
        Map<String, Object> readyData = Map.of(
                "ready", true,
                "timestamp", Instant.now());

        return ResponseEntity.ok(ApiResponse.success(readyData));
    }

    private boolean isPresent(String key) {
        return key != null && !key.isBlank();
    }
}
