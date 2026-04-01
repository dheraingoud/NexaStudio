package com.nexastudio.ai;

import java.time.Duration;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexastudio.ai.GeminiDto.Candidate;
import com.nexastudio.ai.GeminiDto.CodeGenerationOutput;
import com.nexastudio.ai.GeminiDto.Content;
import com.nexastudio.ai.GeminiDto.GeminiRequest;
import com.nexastudio.ai.GeminiDto.GeminiResponse;
import com.nexastudio.ai.GeminiDto.GenerationConfig;
import com.nexastudio.ai.GeminiDto.Part;
import com.nexastudio.ai.GeminiDto.SafetySetting;
import com.nexastudio.ai.GeminiDto.SystemInstruction;
import com.nexastudio.common.exception.AiGenerationException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

/**
 * Client for communicating with Google's Gemini API.
 * Handles request formatting, retries, and response parsing.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.model}")
    private String model;

    @Value("${gemini.api.model-fallback:gemini-2.5-flash}")
    private String fallbackModel;

    @Value("${gemini.api.max-tokens}")
    private int maxTokens;

    @Value("${gemini.api.temperature}")
    private double temperature;

    public GeminiClient(@Qualifier("geminiWebClient") WebClient webClient, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
    }

    /**
     * Generate content using Gemini API
     */
    public GeminiResponse generate(String systemPrompt, String userPrompt) {
        GeminiRequest request = buildRequest(systemPrompt, userPrompt, false);
        return executeRequest(request);
    }

    /**
     * Generate JSON content using Gemini API
     */
    public GeminiResponse generateJson(String systemPrompt, String userPrompt) {
        GeminiRequest request = buildRequest(systemPrompt, userPrompt, true);
        return executeRequest(request);
    }

    /**
     * Generate code and parse as structured output
     */
    public CodeGenerationOutput generateCode(String systemPrompt, String userPrompt) {
        GeminiResponse response = generateJson(systemPrompt, userPrompt);
        return parseCodeGenerationOutput(response);
    }

    /**
     * Build the API request
     */
    private GeminiRequest buildRequest(String systemPrompt, String userPrompt, boolean jsonOutput) {
        GenerationConfig.GenerationConfigBuilder configBuilder = GenerationConfig.builder()
                .temperature(temperature)
                .topK(40)
                .topP(0.95)
                .maxOutputTokens(maxTokens);

        if (jsonOutput) {
            configBuilder.responseMimeType("application/json");
        }

        return GeminiRequest.builder()
                .systemInstruction(SystemInstruction.builder()
                        .parts(List.of(Part.builder().text(systemPrompt).build()))
                        .build())
                .contents(List.of(
                        Content.builder()
                                .role("user")
                                .parts(List.of(Part.builder().text(userPrompt).build()))
                                .build()))
                .generationConfig(configBuilder.build())
                .safetySettings(buildSafetySettings())
                .build();
    }

    /**
     * Build safety settings for the request
     */
    private List<SafetySetting> buildSafetySettings() {
        return List.of(
                SafetySetting.builder()
                        .category("HARM_CATEGORY_HARASSMENT")
                        .threshold("BLOCK_ONLY_HIGH")
                        .build(),
                SafetySetting.builder()
                        .category("HARM_CATEGORY_HATE_SPEECH")
                        .threshold("BLOCK_ONLY_HIGH")
                        .build(),
                SafetySetting.builder()
                        .category("HARM_CATEGORY_SEXUALLY_EXPLICIT")
                        .threshold("BLOCK_ONLY_HIGH")
                        .build(),
                SafetySetting.builder()
                        .category("HARM_CATEGORY_DANGEROUS_CONTENT")
                        .threshold("BLOCK_ONLY_HIGH")
                        .build());
    }

    /**
     * Execute the API request with retries and model fallback
     */
    private GeminiResponse executeRequest(GeminiRequest request) {
        try {
            return executeWithModel(request, model);
        } catch (AiGenerationException e) {
            log.warn("Primary model {} failed, trying {}", model, fallbackModel);
            try {
                return executeWithModel(request, fallbackModel);
            } catch (AiGenerationException fallbackError) {
                throw fallbackError;
            }
        }
    }

    /**
     * Execute request with specific model
     */
    private GeminiResponse executeWithModel(GeminiRequest request, String modelName) {
        String endpoint = "/models/" + modelName + ":generateContent?key=" + apiKey;

        try {
            GeminiResponse response = webClient.post()
                    .uri(endpoint)
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Gemini API 4xx error with model {}: {}", modelName, body);
                                return Mono.error(new AiGenerationException("AI API error: " + body));
                            }))
                    .onStatus(HttpStatusCode::is5xxServerError, resp -> resp.bodyToMono(String.class)
                            .flatMap(body -> {
                                log.error("Gemini API 5xx error with model {}: {}", modelName, body);
                                return Mono.error(new AiGenerationException("AI service temporarily unavailable"));
                            }))
                    .bodyToMono(GeminiResponse.class)
                    .retryWhen(Retry.backoff(2, Duration.ofSeconds(1))
                            .filter(throwable -> throwable instanceof WebClientResponseException.ServiceUnavailable ||
                                    throwable instanceof WebClientResponseException.TooManyRequests)
                            .doBeforeRetry(signal -> log.warn("Retrying Gemini API call with {}, attempt: {}",
                                    modelName, signal.totalRetries() + 1)))
                    .block();

            log.info("Successfully generated with model: {}", modelName);
            return response;
        } catch (Exception e) {
            log.error("Gemini API call failed with model {}", modelName, e);
            throw new AiGenerationException("Failed to generate content with " + modelName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Parse the AI response as CodeGenerationOutput
     */
    private CodeGenerationOutput parseCodeGenerationOutput(GeminiResponse response) {
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            throw new AiGenerationException("Empty response from AI");
        }

        Candidate candidate = response.getCandidates().get(0);

        if (candidate.getContent() == null || candidate.getContent().getParts() == null ||
                candidate.getContent().getParts().isEmpty()) {
            throw new AiGenerationException("No content in AI response");
        }

        String text = candidate.getContent().getParts().get(0).getText();

        try {
            // Clean JSON if wrapped in code blocks
            text = cleanJsonResponse(text);
            return JsonRepair.parse(text, CodeGenerationOutput.class, objectMapper);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse AI response as JSON (first 500 chars): {}",
                    text.substring(0, Math.min(500, text.length())), e);
            throw new AiGenerationException("Failed to parse AI response: " + e.getMessage());
        }
    }

    /**
     * Clean JSON response from markdown code blocks
     */
    private String cleanJsonResponse(String text) {
        if (text == null)
            return null;

        text = text.trim();

        // Remove ```json and ``` markers
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }

        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }

        return text.trim();
    }

    /**
     * Extract text from Gemini response
     */
    public String extractText(GeminiResponse response) {
        if (response == null || response.getCandidates() == null || response.getCandidates().isEmpty()) {
            return null;
        }

        Candidate candidate = response.getCandidates().get(0);
        if (candidate.getContent() == null || candidate.getContent().getParts() == null ||
                candidate.getContent().getParts().isEmpty()) {
            return null;
        }

        return candidate.getContent().getParts().get(0).getText();
    }

    /**
     * Get token usage from response
     */
    public int getTokenUsage(GeminiResponse response) {
        if (response != null && response.getUsageMetadata() != null) {
            return response.getUsageMetadata().getTotalTokenCount();
        }
        return 0;
    }
}
