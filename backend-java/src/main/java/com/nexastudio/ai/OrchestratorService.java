package com.nexastudio.ai;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.nexastudio.ai.GeminiDto.CodeGenerationOutput;
import com.nexastudio.ai.GeminiDto.FileOperation;
import com.nexastudio.common.RateLimitService;
import com.nexastudio.common.exception.AiGenerationException;
import com.nexastudio.common.exception.NexaStudioException;
import com.nexastudio.project.FileEntity;
import com.nexastudio.project.FileRepository;
import com.nexastudio.project.ProjectEntity;
import com.nexastudio.project.ProjectService;
import com.nexastudio.project.PromptEntity;
import com.nexastudio.project.dto.GenerateRequest;
import com.nexastudio.project.dto.GenerateResponse;

/**
 * AI Orchestrator Service.
 * Coordinates the entire AI generation pipeline:
 * Prompt → Context → AI (Kimi primary / Gemini fallback) → Validate → Apply → Save
 */
@Service
public class OrchestratorService {

    private static final Logger log = LoggerFactory.getLogger(OrchestratorService.class);

    private final ProjectService projectService;
    private final AiRouterService aiRouter;
    private final ContextBuilder contextBuilder;
    private final PromptTemplates promptTemplates;
    private final SchemaValidator schemaValidator;
    private final FileRepository fileRepository;
    private final RateLimitService rateLimitService;

    private static final int MAX_RETRIES = 3;
    private static final int MAX_PATH_LENGTH = 500;
    private static final int MAX_FILE_NAME_LENGTH = 100;

    public OrchestratorService(ProjectService projectService, AiRouterService aiRouter,
                               ContextBuilder contextBuilder, PromptTemplates promptTemplates,
                               SchemaValidator schemaValidator, FileRepository fileRepository,
                               RateLimitService rateLimitService) {
        this.projectService = projectService;
        this.aiRouter = aiRouter;
        this.contextBuilder = contextBuilder;
        this.promptTemplates = promptTemplates;
        this.schemaValidator = schemaValidator;
        this.fileRepository = fileRepository;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Main generation entry point
     */
    public GenerateResponse generate(UUID userId, UUID projectId, GenerateRequest request) {
        // Check AI rate limit
        RateLimitService.RateLimitResult rateLimitResult = rateLimitService.checkAiRateLimit(userId);
        if (!rateLimitResult.isAllowed()) {
            throw new NexaStudioException(
                "AI_RATE_LIMIT_EXCEEDED",
                "Rate limit exceeded. Retry in " + rateLimitResult.getRetryAfterSeconds() + "s.",
                HttpStatus.TOO_MANY_REQUESTS
            );
        }

        long startTime = System.currentTimeMillis();

        // Get project
        ProjectEntity project = projectService.getProjectEntity(userId, projectId);

        // Determine intent — auto-detect MODIFY when project already has files
        PromptEntity.PromptIntent intent = parseIntent(request.getIntent());
        if (intent == PromptEntity.PromptIntent.GENERATE) {
            List<FileEntity> existingFiles = fileRepository.findByProjectIdOrderByPathAsc(project.getId());
            if (!existingFiles.isEmpty()) {
                intent = PromptEntity.PromptIntent.MODIFY;
                log.info("Auto-detected MODIFY intent (project has {} existing files)", existingFiles.size());
            }
        }

        // Save prompt
        PromptEntity prompt = projectService.savePrompt(project, request.getPrompt(), intent);

        try {
            // Build context
            String context = contextBuilder.buildContextForIntent(project, intent, request.getTargetFile());

            // Get appropriate system prompt (framework-aware)
            String systemPrompt = promptTemplates.getSystemPromptForIntent(intent.name(), project.getType());

            // Build user prompt with project type context
            String userPrompt = promptTemplates.buildUserPrompt(request.getPrompt(), context, project.getType());

            // Generate with retries — uses Kimi K2.5 primary, Gemini fallback
            CodeGenerationOutput output = generateWithRetry(systemPrompt, userPrompt, MAX_RETRIES);

            // Validate output
            SchemaValidator.ValidationResult validation = schemaValidator.validate(output);
            if (!validation.valid()) {
                throw new AiGenerationException("AI output validation failed: " +
                        String.join(", ", validation.errors()));
            }

            // Log warnings
            if (!validation.warnings().isEmpty()) {
                log.debug("AI output warnings: {}", validation.warnings());
            }

            // Apply changes
            List<GenerateResponse.FileChange> changes = applyChanges(project, output);

            // Update project
            project.setLastGeneratedAt(Instant.now());

            // Calculate processing time
            long processingTime = System.currentTimeMillis() - startTime;

            // Update prompt status
            projectService.updatePromptStatus(
                    prompt.getId(),
                    PromptEntity.PromptStatus.COMPLETED,
                    output.getExplanation(),
                    null, // tokens
                    processingTime,
                    null
            );

            // Build response
            return GenerateResponse.builder()
                    .promptId(prompt.getId())
                    .status("COMPLETED")
                    .changes(changes)
                    .explanation(output.getExplanation())
                    .processingTimeMs(processingTime)
                    .build();

        } catch (Exception e) {
            log.error("Generation failed for project {}: {}", projectId, e.getMessage(), e);

            // Update prompt status
            projectService.updatePromptStatus(
                    prompt.getId(),
                    PromptEntity.PromptStatus.FAILED,
                    null,
                    null,
                    System.currentTimeMillis() - startTime,
                    e.getMessage()
            );

            throw e;
        }
    }

    /**
     * SSE event types emitted during streaming generation.
     */
    public record SseEvent(String type, Object data) {}

    /**
     * Streaming generation entry point — emits SSE events to the callback as generation progresses.
     * Events: "thinking", "file", "complete", "error"
     */
    public void generateStreaming(UUID userId, UUID projectId, GenerateRequest request, Consumer<SseEvent> emitter) {
        // Check AI rate limit
        RateLimitService.RateLimitResult rateLimitResult = rateLimitService.checkAiRateLimit(userId);
        if (!rateLimitResult.isAllowed()) {
            emitter.accept(new SseEvent("error", "Rate limit exceeded. Retry in " + rateLimitResult.getRetryAfterSeconds() + "s."));
            return;
        }

        long startTime = System.currentTimeMillis();

        // Get project
        ProjectEntity project = projectService.getProjectEntity(userId, projectId);

        // Determine intent — auto-detect MODIFY when project already has files
        PromptEntity.PromptIntent intent = parseIntent(request.getIntent());
        if (intent == PromptEntity.PromptIntent.GENERATE) {
            List<FileEntity> existingFiles = fileRepository.findByProjectIdOrderByPathAsc(project.getId());
            if (!existingFiles.isEmpty()) {
                intent = PromptEntity.PromptIntent.MODIFY;
                log.info("Auto-detected MODIFY intent (project has {} existing files)", existingFiles.size());
            }
        }

        // Save prompt
        PromptEntity prompt = projectService.savePrompt(project, request.getPrompt(), intent);

        try {
            emitter.accept(new SseEvent("thinking", "Analyzing your request..."));

            // Build context
            String context = contextBuilder.buildContextForIntent(project, intent, request.getTargetFile());
            emitter.accept(new SseEvent("thinking", "Understanding project context..."));

            emitter.accept(new SseEvent("thinking", "Planning file structure..."));

            // Get appropriate system prompt (framework-aware)
            String systemPrompt = promptTemplates.getSystemPromptForIntent(intent.name(), project.getType());

            // Build user prompt with project type context
            String userPrompt = promptTemplates.buildUserPrompt(request.getPrompt(), context, project.getType());

            emitter.accept(new SseEvent("thinking", "Generating code with AI..."));

            // Generate with retries
            CodeGenerationOutput output = generateWithRetry(systemPrompt, userPrompt, MAX_RETRIES);

            // Validate output
            SchemaValidator.ValidationResult validation = schemaValidator.validate(output);
            if (!validation.valid()) {
                throw new AiGenerationException("AI output validation failed: " +
                        String.join(", ", validation.errors()));
            }

            emitter.accept(new SseEvent("thinking", "Writing files..."));

            // Apply changes one-by-one, emitting each file as it's saved
            List<GenerateResponse.FileChange> changes = new ArrayList<>();
            List<String> writeErrors = new ArrayList<>();
            if (output.getFiles() != null) {
                for (FileOperation fileOp : output.getFiles()) {
                    String path = normalizePath(fileOp.getPath());
                    String action = fileOp.getAction().toUpperCase();
                    String content = schemaValidator.sanitizeContent(fileOp.getContent());

                    if (path == null) {
                        writeErrors.add("Invalid or unsupported file path from AI output");
                        continue;
                    }

                    try {
                        switch (action) {
                            case "CREATE", "UPDATE" -> {
                                FileEntity file = fileRepository.findByProjectIdAndPath(project.getId(), path)
                                        .orElse(FileEntity.builder()
                                                .project(project)
                                                .path(path)
                                                .name(truncateFileName(extractFileName(path)))
                                                .build());
                                file.setContent(content);
                                file.setName(truncateFileName(extractFileName(path)));
                                file.setGenerated(true);
                                fileRepository.save(file);
                                log.info("Applied {} to file: {}", action, path);
                            }
                            case "DELETE" -> {
                                fileRepository.findByProjectIdAndPath(project.getId(), path)
                                        .ifPresent(f -> {
                                            if (!f.isLocked()) {
                                                fileRepository.delete(f);
                                                log.info("Deleted file: {}", path);
                                            }
                                        });
                            }
                                    default -> {
                                        writeErrors.add("Unsupported action '" + action + "' for " + path);
                                        continue;
                                    }
                        }

                        GenerateResponse.FileChange change = GenerateResponse.FileChange.builder()
                                .path(path)
                                .action(action)
                                .content(action.equals("DELETE") ? null : content)
                                .summary(fileOp.getSummary())
                                .build();
                        changes.add(change);

                        // Emit the file event immediately
                        emitter.accept(new SseEvent("file", change));

                    } catch (Exception e) {
                        String error = "Failed to apply change to " + path + ": " + e.getMessage();
                        log.error(error);
                        writeErrors.add(error);
                    }
                }
            }

            if (!writeErrors.isEmpty()) {
                throw new AiGenerationException("Failed to save generated files: " + String.join(" | ", writeErrors));
            }

            // Update project
            project.setLastGeneratedAt(Instant.now());

            long processingTime = System.currentTimeMillis() - startTime;

            // Update prompt status
            projectService.updatePromptStatus(
                    prompt.getId(),
                    PromptEntity.PromptStatus.COMPLETED,
                    output.getExplanation(),
                    null,
                    processingTime,
                    null
            );

            // Emit completion
            GenerateResponse finalResponse = GenerateResponse.builder()
                    .promptId(prompt.getId())
                    .status("COMPLETED")
                    .changes(changes)
                    .explanation(output.getExplanation())
                    .processingTimeMs(processingTime)
                    .build();

            emitter.accept(new SseEvent("complete", finalResponse));

        } catch (Exception e) {
            log.error("Streaming generation failed for project {}: {}", projectId, e.getMessage(), e);

            projectService.updatePromptStatus(
                    prompt.getId(),
                    PromptEntity.PromptStatus.FAILED,
                    null,
                    null,
                    System.currentTimeMillis() - startTime,
                    e.getMessage()
            );

            emitter.accept(new SseEvent("error", e.getMessage()));
        }
    }

    /**
     * Generate with retry logic — routes through AiRouterService (Kimi → Gemini)
     */
    private CodeGenerationOutput generateWithRetry(String systemPrompt, String userPrompt, int maxRetries) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("AI generation attempt {}/{}", attempt, maxRetries);
                return aiRouter.generateCode(systemPrompt, userPrompt);
            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed: {}", attempt, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000L * attempt); // backoff: 1s, 2s, 3s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new AiGenerationException("Generation interrupted", ie);
                    }
                }
            }
        }

        throw new AiGenerationException("Generation failed after " + maxRetries + " attempts: " +
                (lastException != null ? lastException.getMessage() : "Unknown error"));
    }

    /**
     * Apply file changes from AI output
     */
    private List<GenerateResponse.FileChange> applyChanges(ProjectEntity project, CodeGenerationOutput output) {
        List<GenerateResponse.FileChange> changes = new ArrayList<>();

        if (output.getFiles() == null) {
            return changes;
        }

        for (FileOperation fileOp : output.getFiles()) {
            String path = normalizePath(fileOp.getPath());
            String action = fileOp.getAction().toUpperCase();
            String content = schemaValidator.sanitizeContent(fileOp.getContent());

            if (path == null) {
                throw new AiGenerationException("AI output included an invalid file path");
            }

            try {
                switch (action) {
                    case "CREATE", "UPDATE" -> {
                        FileEntity file = fileRepository.findByProjectIdAndPath(project.getId(), path)
                                .orElse(FileEntity.builder()
                                        .project(project)
                                        .path(path)
                                        .name(truncateFileName(extractFileName(path)))
                                        .build());

                        file.setContent(content);
                        file.setName(truncateFileName(extractFileName(path)));
                        file.setGenerated(true);
                        fileRepository.save(file);

                        log.info("Applied {} to file: {}", action, path);
                    }
                    case "DELETE" -> {
                        fileRepository.findByProjectIdAndPath(project.getId(), path)
                                .ifPresent(file -> {
                                    if (!file.isLocked()) {
                                        fileRepository.delete(file);
                                        log.info("Deleted file: {}", path);
                                    }
                                });
                    }
                    default -> throw new AiGenerationException("Unsupported file action: " + action);
                }

                changes.add(GenerateResponse.FileChange.builder()
                        .path(path)
                        .action(action)
                        .content(action.equals("DELETE") ? null : content)
                        .summary(fileOp.getSummary())
                        .build());

            } catch (Exception e) {
                throw new AiGenerationException("Failed to apply change to file " + path + ": " + e.getMessage(), e);
            }
        }

        return changes;
    }

    private String normalizePath(String rawPath) {
        if (rawPath == null) {
            return null;
        }

        String normalized = rawPath.trim().replace('\\', '/').replaceAll("/{2,}", "/");
        if (normalized.isEmpty()) {
            return null;
        }

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        if (normalized.length() > MAX_PATH_LENGTH) {
            log.warn("Skipping file path longer than {} chars: {}", MAX_PATH_LENGTH, normalized);
            return null;
        }

        return normalized;
    }

    private String extractFileName(String path) {
        int lastSlash = path.lastIndexOf('/');
        if (lastSlash < 0 || lastSlash == path.length() - 1) {
            return path;
        }
        return path.substring(lastSlash + 1);
    }

    private String truncateFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "file";
        }

        if (fileName.length() <= MAX_FILE_NAME_LENGTH) {
            return fileName;
        }

        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            String extension = fileName.substring(dotIndex);
            int baseMax = Math.max(1, MAX_FILE_NAME_LENGTH - extension.length());
            return fileName.substring(0, baseMax) + extension;
        }

        return fileName.substring(0, MAX_FILE_NAME_LENGTH);
    }

    /**
     * Parse intent from string
     */
    private PromptEntity.PromptIntent parseIntent(String intent) {
        if (intent == null || intent.isEmpty()) {
            return PromptEntity.PromptIntent.GENERATE;
        }

        try {
            return PromptEntity.PromptIntent.valueOf(intent.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PromptEntity.PromptIntent.GENERATE;
        }
    }
}
