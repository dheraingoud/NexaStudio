package com.nexastudio.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexastudio.ai.GeminiDto.CodeGenerationOutput;
import com.nexastudio.common.exception.AiGenerationException;

import jakarta.annotation.PostConstruct;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

/**
 * AI Router Service — Cascading fallback through NVIDIA NIM models.
 *
 * Strategy (single API key for all NVIDIA models):
 * 1. MiniMax M2.5 (primary - powerful reasoning)
 * 2. Step-3.5-Flash (fast, versatile)
 * 3. Kimi K2.5 (thorough, code-focused)
 * 4. Gemini (final fallback)
 */
@Service
public class AiRouterService {

    private static final Logger log = LoggerFactory.getLogger(AiRouterService.class);

    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    private final WebClient stepWebClient;
    private final WebClient minimaxWebClient;
    private final WebClient kimiWebClient;

    @Value("${nvidia.api.key:}")
    private String nvidiaApiKey;

    @Value("${nvidia.step.model:stepfun/step-3.5-flash}")
    private String stepModel;
    @Value("${nvidia.step.max-tokens:16384}")
    private int stepMaxTokens;
    @Value("${nvidia.step.temperature:0.7}")
    private double stepTemp;

    @Value("${nvidia.minimax.model:minimax/minimax-m2.5}")
    private String minimaxModel;
    @Value("${nvidia.minimax.max-tokens:16384}")
    private int minimaxMaxTokens;
    @Value("${nvidia.minimax.temperature:0.7}")
    private double minimaxTemp;

    @Value("${nvidia.kimi.model:moonshotai/kimi-k2.5}")
    private String kimiModel;
    @Value("${nvidia.kimi.max-tokens:16384}")
    private int kimiMaxTokens;
    @Value("${nvidia.kimi.temperature:0.7}")
    private double kimiTemp;

    private NvidiaModelClient stepClient;
    private NvidiaModelClient minimaxClient;
    private NvidiaModelClient kimiClient;

    public AiRouterService(
            GeminiClient geminiClient,
            ObjectMapper objectMapper,
            @Qualifier("stepWebClient") WebClient stepWebClient,
            @Qualifier("minimaxWebClient") WebClient minimaxWebClient,
            @Qualifier("kimiWebClient") WebClient kimiWebClient) {
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
        this.stepWebClient = stepWebClient;
        this.minimaxWebClient = minimaxWebClient;
        this.kimiWebClient = kimiWebClient;
    }

    @PostConstruct
    public void init() {
        boolean hasNvidiaKey = isKeyPresent(nvidiaApiKey);
        log.info("NVIDIA API Key: {}", hasNvidiaKey ? "✓ configured" : "✗ missing");

        // Build model clients with NVIDIA NIM-specific thinking modes
        minimaxClient = new NvidiaModelClient("MiniMax-M2.5", minimaxModel, minimaxMaxTokens, minimaxTemp,
                minimaxWebClient, objectMapper, Map.of("enable_thinking", true));

        stepClient = new NvidiaModelClient("Step-3.5-Flash", stepModel, stepMaxTokens, stepTemp,
                stepWebClient, objectMapper, Map.of("enable_thinking", true));

        kimiClient = new NvidiaModelClient("Kimi-K2.5", kimiModel, kimiMaxTokens, kimiTemp,
                kimiWebClient, objectMapper, Map.of("reasoning_effort", "high"));

        log.info("AI Router ready — MiniMax M2.5 → Step-3.5-Flash → Kimi K2.5 → Gemini");
    }

    /**
     * Generate code using cascading fallback strategy.
     * All NVIDIA models use the same API key.
     */
    public CodeGenerationOutput generateCode(String systemPrompt, String userPrompt) {

        boolean hasNvidiaKey = isKeyPresent(nvidiaApiKey);

        if (hasNvidiaKey) {
            // ── Tier 1: MiniMax M2.5 (primary - powerful reasoning) ─────────────
            try {
                long start = System.currentTimeMillis();
                log.info("🚀 Trying MiniMax M2.5 (primary)...");
                CodeGenerationOutput result = minimaxClient.generateCode(systemPrompt, userPrompt);
                int fileCount = result.getFiles() != null ? result.getFiles().size() : 0;
                log.info("✅ MiniMax M2.5 completed in {}ms — {} files",
                        System.currentTimeMillis() - start, fileCount);
                return result;
            } catch (Exception e) {
                log.warn("⚠️ MiniMax M2.5 failed: {}", e.getMessage());
            }

            // ── Tier 2: Step-3.5-Flash (fast, versatile) ─────────────
            try {
                long start = System.currentTimeMillis();
                log.info("🔄 Trying Step-3.5-Flash...");
                CodeGenerationOutput result = stepClient.generateCode(systemPrompt, userPrompt);
                int fileCount = result.getFiles() != null ? result.getFiles().size() : 0;
                log.info("✅ Step-3.5-Flash completed in {}ms — {} files",
                        System.currentTimeMillis() - start, fileCount);
                return result;
            } catch (Exception e) {
                log.warn("⚠️ Step-3.5-Flash failed: {}", e.getMessage());
            }

            // ── Tier 3: Kimi K2.5 (thorough, code-focused) ─────────────
            try {
                long start = System.currentTimeMillis();
                log.info("🔄 Trying Kimi K2.5...");
                CodeGenerationOutput result = kimiClient.generateCode(systemPrompt, userPrompt);
                int fileCount = result.getFiles() != null ? result.getFiles().size() : 0;
                log.info("✅ Kimi K2.5 completed in {}ms — {} files",
                        System.currentTimeMillis() - start, fileCount);
                return result;
            } catch (Exception e) {
                log.warn("⚠️ Kimi K2.5 failed: {}", e.getMessage());
            }
        } else {
            log.warn("⚠️ NVIDIA API key not configured, using Gemini directly...");
        }

        // ── Tier 4: Gemini (final fallback) ─────────────
        try {
            long start = System.currentTimeMillis();
            log.info("🔄 Trying Gemini as final fallback...");
            CodeGenerationOutput result = geminiClient.generateCode(systemPrompt, userPrompt);
            int fileCount = result.getFiles() != null ? result.getFiles().size() : 0;
            log.info("✅ Gemini completed in {}ms — {} files",
                    System.currentTimeMillis() - start, fileCount);
            return result;
        } catch (Exception e) {
            log.error("❌ All AI providers failed. Last error: {}", e.getMessage());
        }

        throw new AiGenerationException("AI generation failed. All providers unavailable.", null);
    }

    private boolean isKeyPresent(String key) {
        return key != null && !key.isBlank();
    }
}
