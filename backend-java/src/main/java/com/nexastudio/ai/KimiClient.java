package com.nexastudio.ai;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexastudio.ai.GeminiDto.CodeGenerationOutput;
import com.nexastudio.common.exception.AiGenerationException;

import jakarta.annotation.PostConstruct;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Client for communicating with Kimi K2.5 via NVIDIA's OpenAI-compatible API.
 * Primary AI model for NexaStudio code generation.
 * Supports thinking mode for enhanced agent-swarm style reasoning.
 */
@Component
public class KimiClient {

    private static final Logger log = LoggerFactory.getLogger(KimiClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${nvidia.kimi.api.key:}")
    private String apiKey;

    @Value("${nvidia.kimi.api.model:moonshotai/kimi-k2.5}")
    private String model;

    @Value("${nvidia.kimi.api.max-tokens:16384}")
    private int maxTokens;

    @Value("${nvidia.kimi.api.temperature:1.0}")
    private double temperature;

    @Value("${nvidia.kimi.api.top-p:1.0}")
    private double topP;

    public KimiClient(@Qualifier("kimiWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        if (isAvailable()) {
            log.info("Kimi K2.5 ready | model={}", model);
        } else {
            log.warn("Kimi API key not configured");
        }
    }

    /**
     * Check if the Kimi API key is configured
     */
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Generate code using Kimi K2.5 and parse as structured CodeGenerationOutput.
     * Uses thinking mode for better reasoning and agent-swarm style planning.
     */
    public CodeGenerationOutput generateCode(String systemPrompt, String userPrompt) {
        String responseText = callApi(systemPrompt, userPrompt);
        return parseCodeGenerationOutput(responseText);
    }

    /**
     * Call the NVIDIA Kimi API (OpenAI-compatible chat completions endpoint).
     * Non-streaming with thinking DISABLED to maximize output token budget.
     * All 16384 tokens go directly to the JSON response content.
     */
    private String callApi(String systemPrompt, String userPrompt) {
        // NVIDIA caps max_tokens at 16384 for Kimi K2.5.
        // With thinking disabled, ALL 16384 tokens go to actual content output.
        int effectiveMaxTokens = Math.min(maxTokens, 16384);

        // Build request with thinking DISABLED.
        // When thinking is disabled, the model skips internal reasoning and
        // puts all tokens toward the JSON response we need.
        Map<String, Object> thinkingConfig = Map.of("type", "disabled");
        Map<String, Object> requestBody = new java.util.LinkedHashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));
        requestBody.put("max_tokens", effectiveMaxTokens);
        requestBody.put("temperature", temperature);
        requestBody.put("top_p", topP);
        requestBody.put("stream", false);
        requestBody.put("thinking", thinkingConfig);

        try {
            log.info("Kimi request: model={}, maxTokens={}, thinking=disabled, stream=false", model,
                    effectiveMaxTokens);

            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Kimi API 4xx error (status {}): {}", resp.statusCode(), body);
                                return Mono.error(new AiGenerationException(
                                        "Kimi API error (" + resp.statusCode() + "): " + body));
                            }))
                    .onStatus(HttpStatusCode::is5xxServerError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Kimi API 5xx error (status {}): {}", resp.statusCode(), body);
                                return Mono.error(new AiGenerationException(
                                        "Kimi API service temporarily unavailable (" + resp.statusCode() + ")"));
                            }))
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(2))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable ||
                                    throwable instanceof WebClientResponseException.TooManyRequests)
                            .doBeforeRetry(signal -> log.warn("Retrying Kimi API call, attempt: {}",
                                    signal.totalRetries() + 1)))
                    .timeout(Duration.ofSeconds(180))
                    .block(Duration.ofSeconds(190));

            log.info("Kimi response received: {} chars", responseBody != null ? responseBody.length() : 0);
            return extractContentFromResponse(responseBody);

        } catch (AiGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Kimi API call failed: {}", e.getMessage(), e);
            throw new AiGenerationException("Failed to generate content with Kimi K2.5: " + e.getMessage(), e);
        }
    }

    /**
     * Extract the assistant's message content from OpenAI-format response.
     * Response format: { choices: [{ message: { content: "..." } }] }
     */
    private String extractContentFromResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiGenerationException("Empty response from Kimi API");
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");

            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new AiGenerationException("No choices in Kimi API response");
            }

            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                throw new AiGenerationException("No message in Kimi API response choice");
            }

            JsonNode content = message.get("content");
            if (content == null || content.isNull()) {
                throw new AiGenerationException("No content in Kimi API response message");
            }

            String text = content.asText();

            // Log thinking/reasoning if present (Kimi thinking mode)
            JsonNode reasoning = message.get("reasoning_content");
            if (reasoning != null && !reasoning.isNull()) {
                log.debug("Kimi reasoning: {}", reasoning.asText().substring(0,
                        Math.min(200, reasoning.asText().length())) + "...");
            }

            // Log token usage if present
            JsonNode usage = root.get("usage");
            if (usage != null) {
                log.debug("Kimi tokens — total: {}",
                        usage.has("total_tokens") ? usage.get("total_tokens").asInt() : "?");
            }

            return text;

        } catch (AiGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to parse Kimi API response: {}", responseBody, e);
            throw new AiGenerationException("Failed to parse Kimi API response: " + e.getMessage());
        }
    }

    /**
     * Parse the response text as CodeGenerationOutput (same format as Gemini).
     */
    private CodeGenerationOutput parseCodeGenerationOutput(String text) {
        if (text == null || text.isBlank()) {
            throw new AiGenerationException("Empty content from Kimi API");
        }

        // Clean JSON response (strip markdown code blocks if present)
        String cleaned = cleanJsonResponse(text);

        try {
            return JsonRepair.parse(cleaned, CodeGenerationOutput.class, objectMapper);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse Kimi response as CodeGenerationOutput. Raw text (first 500): {}",
                    cleaned.substring(0, Math.min(500, cleaned.length())), e);
            throw new AiGenerationException("Failed to parse Kimi AI response as JSON: " + e.getMessage());
        }
    }

    /**
     * Clean JSON response from markdown code blocks and extract embedded JSON.
     */
    private String cleanJsonResponse(String text) {
        if (text == null)
            return null;

        text = text.trim();

        // Remove ```json ... ``` markers (handle multiple possible formats)
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```JSON")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }

        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }

        text = text.trim();

        // If still not starting with { or [, try to extract JSON object
        if (!text.startsWith("{") && !text.startsWith("[")) {
            int jsonStart = text.indexOf('{');
            int jsonEnd = text.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                text = text.substring(jsonStart, jsonEnd + 1);
                log.debug("Extracted embedded JSON from Kimi response (chars {}-{})", jsonStart, jsonEnd);
            }
        }

        return text.trim();
    }
}
