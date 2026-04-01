package com.nexastudio.ai;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexastudio.ai.GeminiDto.CodeGenerationOutput;
import com.nexastudio.common.exception.AiGenerationException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Generic client for any NVIDIA-hosted AI model (OpenAI-compatible API).
 * Used by AiRouterService to talk to Qwen, GLM, DeepSeek, Kimi, etc.
 * All models share the same /chat/completions endpoint format.
 */
public class NvidiaModelClient {

    private static final Logger log = LoggerFactory.getLogger(NvidiaModelClient.class);

    private final String displayName;
    private final String modelId;
    private final int maxTokens;
    private final double temperature;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Map<String, Object> chatTemplateKwargs;

    public NvidiaModelClient(String displayName, String modelId, int maxTokens,
            double temperature, WebClient webClient, ObjectMapper objectMapper,
            Map<String, Object> chatTemplateKwargs) {
        this.displayName = displayName;
        this.modelId = modelId;
        this.maxTokens = maxTokens;
        this.temperature = temperature;
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.chatTemplateKwargs = chatTemplateKwargs;
    }

    public NvidiaModelClient(String displayName, String modelId, int maxTokens,
            double temperature, WebClient webClient, ObjectMapper objectMapper) {
        this(displayName, modelId, maxTokens, temperature, webClient, objectMapper, null);
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Generate code: call the NVIDIA API and parse as CodeGenerationOutput.
     */
    public CodeGenerationOutput generateCode(String systemPrompt, String userPrompt) {
        String responseText = callApi(systemPrompt, userPrompt);
        return parseCodeGenerationOutput(responseText);
    }

    /**
     * Call the NVIDIA chat/completions endpoint.
     * Thinking mode is ENABLED via model-specific chat_template_kwargs.
     * max_tokens capped at 16384 per NVIDIA API limits.
     */
    private String callApi(String systemPrompt, String userPrompt) {
        Map<String, Object> requestBody = new java.util.LinkedHashMap<>();
        requestBody.put("model", modelId);
        requestBody.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", userPrompt)));
        requestBody.put("max_tokens", Math.min(maxTokens, 16384));
        requestBody.put("temperature", temperature);
        requestBody.put("top_p", 0.95);
        requestBody.put("stream", false);

        if (chatTemplateKwargs != null && !chatTemplateKwargs.isEmpty()) {
            requestBody.put("chat_template_kwargs", chatTemplateKwargs);
        }

        try {
            log.info("[{}] Request: model={}, maxTokens={}, thinkingKwargs={}",
                    displayName, modelId, Math.min(maxTokens, 16384), chatTemplateKwargs);

            String responseBody = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("[{}] 4xx error ({}): {}", displayName, resp.statusCode(), body);
                                return Mono.error(new AiGenerationException(
                                        displayName + " API error (" + resp.statusCode() + "): " + body));
                            }))
                    .onStatus(HttpStatusCode::is5xxServerError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("[{}] 5xx error ({}): {}", displayName, resp.statusCode(), body);
                                return Mono.error(new AiGenerationException(
                                        displayName + " API unavailable (" + resp.statusCode() + ")"));
                            }))
                    .bodyToMono(String.class)
                    .retryWhen(Retry.backoff(1, Duration.ofSeconds(2))
                            .filter(t -> t instanceof WebClientResponseException.ServiceUnavailable ||
                                    t instanceof WebClientResponseException.TooManyRequests)
                            .doBeforeRetry(sig -> log.warn("[{}] Retrying (attempt {})",
                                    displayName, sig.totalRetries() + 1)))
                    .timeout(Duration.ofSeconds(180))
                    .block(Duration.ofSeconds(190));

            log.info("[{}] Response received: {} chars", displayName,
                    responseBody != null ? responseBody.length() : 0);
            return extractContentFromResponse(responseBody);

        } catch (AiGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[{}] API call failed: {}", displayName, e.getMessage());
            throw new AiGenerationException(
                    "Failed to generate with " + displayName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Extract assistant content from OpenAI-format response.
     */
    private String extractContentFromResponse(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            throw new AiGenerationException("Empty response from " + displayName);
        }

        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choices = root.get("choices");

            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new AiGenerationException("No choices in " + displayName + " response");
            }

            JsonNode message = choices.get(0).get("message");
            if (message == null) {
                throw new AiGenerationException("No message in " + displayName + " response");
            }

            JsonNode content = message.get("content");
            if (content == null || content.isNull()) {
                throw new AiGenerationException("No content in " + displayName + " response");
            }

            // Log token usage if present
            JsonNode usage = root.get("usage");
            if (usage != null) {
                log.debug("[{}] Tokens — total: {}", displayName,
                        usage.has("total_tokens") ? usage.get("total_tokens").asInt() : "?");
            }

            return content.asText();

        } catch (AiGenerationException e) {
            throw e;
        } catch (Exception e) {
            log.error("[{}] Failed to parse response: {}", displayName,
                    responseBody.substring(0, Math.min(300, responseBody.length())), e);
            throw new AiGenerationException("Failed to parse " + displayName + " response: " + e.getMessage());
        }
    }

    /**
     * Parse response text as CodeGenerationOutput.
     */
    private CodeGenerationOutput parseCodeGenerationOutput(String text) {
        if (text == null || text.isBlank()) {
            throw new AiGenerationException("Empty content from " + displayName);
        }

        String cleaned = cleanJsonResponse(text);

        try {
            return JsonRepair.parse(cleaned, CodeGenerationOutput.class, objectMapper);
        } catch (JsonProcessingException e) {
            log.error("[{}] JSON parse failed. Raw (first 500): {}", displayName,
                    cleaned.substring(0, Math.min(500, cleaned.length())), e);
            throw new AiGenerationException("Failed to parse " + displayName + " JSON: " + e.getMessage());
        }
    }

    /**
     * Strip markdown code fences and extract embedded JSON.
     */
    private String cleanJsonResponse(String text) {
        if (text == null)
            return null;
        text = text.trim();

        if (text.startsWith("```json") || text.startsWith("```JSON")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }

        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }

        text = text.trim();

        if (!text.startsWith("{") && !text.startsWith("[")) {
            int jsonStart = text.indexOf('{');
            int jsonEnd = text.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                text = text.substring(jsonStart, jsonEnd + 1);
            }
        }

        return text.trim();
    }
}
