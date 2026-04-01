package com.nexastudio.ai;

import com.nexastudio.ai.GeminiDto.CodeGenerationOutput;
import com.nexastudio.ai.GeminiDto.FileOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Schema Validator Service.
 * Validates AI output for security, correctness, and safety.
 */
@Service
public class SchemaValidator {

    private static final Logger log = LoggerFactory.getLogger(SchemaValidator.class);

    // Protected paths that cannot be modified
    private static final Set<String> PROTECTED_PATHS = Set.of(
            "/node_modules",
            "/.git",
            "/.env",
            "/.env.local",
            "/package-lock.json");

    // Allowed file extensions
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".ts", ".tsx", ".js", ".jsx",
            ".json", ".css", ".scss",
            ".html", ".md", ".txt",
            ".svg", ".ico");

    // Forbidden patterns in code — only truly dangerous ones
    private static final List<Pattern> FORBIDDEN_PATTERNS = List.of(
            Pattern.compile("eval\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("new\\s+Function\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("document\\.write\\s*\\(", Pattern.CASE_INSENSITIVE),
            Pattern.compile("require\\s*\\(\\s*[\"']child_process", Pattern.CASE_INSENSITIVE),
            Pattern.compile("require\\s*\\(\\s*[\"']fs[\"']\\s*\\)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("import.*from\\s*[\"']fs[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("__proto__", Pattern.CASE_INSENSITIVE),
            Pattern.compile("constructor\\s*\\[", Pattern.CASE_INSENSITIVE));

    /**
     * Validate and sanitize AI output
     */
    public ValidationResult validate(CodeGenerationOutput output) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (output == null) {
            errors.add("AI output is null");
            return new ValidationResult(false, errors, warnings);
        }

        if (output.getFiles() == null || output.getFiles().isEmpty()) {
            warnings.add("No files in output");
            return new ValidationResult(true, errors, warnings);
        }

        List<FileOperation> safeFiles = new ArrayList<>();
        for (FileOperation file : output.getFiles()) {
            ValidationOutcome outcome = validateFileOperation(file, errors, warnings);
            if (outcome == ValidationOutcome.VALID) {
                safeFiles.add(file);
            }
        }
        output.setFiles(safeFiles);

        boolean valid = errors.isEmpty();
        if (!valid) {
            log.warn("Validation failed: {}", errors);
        }

        return new ValidationResult(valid, errors, warnings);
    }

    /**
     * Validate a single file operation
     */
    private ValidationOutcome validateFileOperation(FileOperation file, List<String> errors, List<String> warnings) {
        String path = file.getPath();
        String content = file.getContent();
        String action = file.getAction();
        int errorCount = errors.size();

        // Validate path
        if (path == null || path.isEmpty()) {
            errors.add("File path is required");
            return ValidationOutcome.ERROR;
        }

        // Normalize path
        if (!path.startsWith("/")) {
            file.setPath("/" + path);
            path = file.getPath();
        }

        // Block path traversal
        if (path.contains("..")) {
            errors.add("Path traversal not allowed: " + path);
            return ValidationOutcome.ERROR;
        }

        // Check protected paths
        for (String protectedPath : PROTECTED_PATHS) {
            if (path.startsWith(protectedPath) || path.contains(protectedPath)) {
                warnings.add("Skipped protected path: " + path);
                return ValidationOutcome.SKIP;
            }
        }

        // Check file extension
        if (!isAllowedExtension(path)) {
            warnings.add("Skipped unsupported extension: " + path);
            return ValidationOutcome.SKIP;
        }

        // Validate action
        if (action == null || action.isEmpty()) {
            file.setAction("CREATE");
            action = "CREATE";
        }

        action = action.toUpperCase();
        if (!Set.of("CREATE", "UPDATE", "DELETE").contains(action)) {
            errors.add("Invalid action for file " + path + ": " + action);
            return ValidationOutcome.ERROR;
        }

        // Validate content for CREATE/UPDATE
        if (!action.equals("DELETE")) {
            if (content == null || content.isEmpty()) {
                errors.add("Content required for " + action + " action: " + path);
                return ValidationOutcome.ERROR;
            }

            // Check for forbidden patterns
            List<String> forbidden = checkForbiddenPatterns(content);
            if (!forbidden.isEmpty()) {
                errors.add("Forbidden code patterns in " + path + ": " + String.join(", ", forbidden));
            }

            // Validate TypeScript/JavaScript syntax (basic)
            if (path.endsWith(".ts") || path.endsWith(".tsx") ||
                    path.endsWith(".js") || path.endsWith(".jsx")) {
                validateJavaScriptSyntax(path, content, errors, warnings);
            }

            // Validate JSON
            if (path.endsWith(".json")) {
                validateJsonSyntax(path, content, errors);
            }
        }

        return errors.size() > errorCount ? ValidationOutcome.ERROR : ValidationOutcome.VALID;
    }

    private enum ValidationOutcome {
        VALID,
        SKIP,
        ERROR
    }

    /**
     * Check if file extension is allowed
     */
    private boolean isAllowedExtension(String path) {
        String lower = path.toLowerCase();
        return ALLOWED_EXTENSIONS.stream().anyMatch(lower::endsWith);
    }

    /**
     * Check for forbidden patterns in code
     */
    private List<String> checkForbiddenPatterns(String content) {
        List<String> found = new ArrayList<>();
        for (Pattern pattern : FORBIDDEN_PATTERNS) {
            if (pattern.matcher(content).find()) {
                found.add(pattern.pattern());
            }
        }
        return found;
    }

    /**
     * Basic JavaScript/TypeScript syntax validation
     */
    private void validateJavaScriptSyntax(String path, String content,
            List<String> errors, List<String> warnings) {
        // Check for balanced braces
        int braces = 0;
        int parens = 0;
        int brackets = 0;

        boolean inString = false;
        char stringChar = 0;
        boolean escaped = false;

        for (char c : content.toCharArray()) {
            if (escaped) {
                escaped = false;
                continue;
            }

            if (c == '\\') {
                escaped = true;
                continue;
            }

            if (!inString) {
                if (c == '"' || c == '\'' || c == '`') {
                    inString = true;
                    stringChar = c;
                } else if (c == '{') {
                    braces++;
                } else if (c == '}') {
                    braces--;
                } else if (c == '(') {
                    parens++;
                } else if (c == ')') {
                    parens--;
                } else if (c == '[') {
                    brackets++;
                } else if (c == ']') {
                    brackets--;
                }
            } else {
                if (c == stringChar) {
                    inString = false;
                }
            }
        }

        if (braces != 0) {
            warnings.add("Unbalanced braces in " + path);
        }
        if (parens != 0) {
            warnings.add("Unbalanced parentheses in " + path);
        }
        if (brackets != 0) {
            warnings.add("Unbalanced brackets in " + path);
        }

        // Check for 'use client' in files with hooks
        if (content.contains("useState") || content.contains("useEffect") ||
                content.contains("useRef") || content.contains("onClick") ||
                content.contains("onChange")) {
            if (!content.contains("'use client'") && !content.contains("\"use client\"")) {
                warnings.add("File " + path + " uses client-side features but missing 'use client' directive");
            }
        }
    }

    /**
     * Validate JSON syntax
     */
    private void validateJsonSyntax(String path, String content, List<String> errors) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            mapper.readTree(content);
        } catch (Exception e) {
            errors.add("Invalid JSON in " + path + ": " + e.getMessage());
        }
    }

    /**
     * Sanitize file content
     */
    public String sanitizeContent(String content) {
        if (content == null)
            return null;

        // Remove any BOM characters
        content = content.replace("\uFEFF", "");

        // Normalize line endings
        content = content.replace("\r\n", "\n").replace("\r", "\n");

        // Remove trailing whitespace from lines
        content = content.replaceAll("[ \\t]+$", "");

        return content;
    }

    /**
     * Validation result holder
     */
    public record ValidationResult(
            boolean valid,
            List<String> errors,
            List<String> warnings) {
    }
}
