package com.nexastudio.ai;

import com.nexastudio.project.FileEntity;
import com.nexastudio.project.FileRepository;
import com.nexastudio.project.ProjectEntity;
import com.nexastudio.project.PromptEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Context Builder Service.
 * Builds the context for AI prompts including relevant files and project
 * structure.
 */
@Service
public class ContextBuilder {

    private final FileRepository fileRepository;

    private static final int MAX_CONTEXT_LENGTH = 100000; // characters
    private static final int MAX_FILES_IN_CONTEXT = 30;

    public ContextBuilder(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
    }

    /**
     * Build full context for a project
     */
    public String buildContext(ProjectEntity project, String targetFile) {
        StringBuilder context = new StringBuilder();

        // Add project info
        context.append("## Project Information\n");
        context.append("Name: ").append(project.getName()).append("\n");
        context.append("Type: ").append(project.getType().name()).append("\n");
        if (project.getDescription() != null) {
            context.append("Description: ").append(project.getDescription()).append("\n");
        }
        context.append("\n");

        // Add file structure
        context.append("## Project File Structure\n");
        List<FileEntity> files = fileRepository.findByProjectIdOrderByPathAsc(project.getId());
        for (FileEntity file : files) {
            context.append("- ").append(file.getPath()).append("\n");
        }
        context.append("\n");

        // Add relevant file contents
        context.append("## Relevant File Contents\n\n");

        // If target file specified, prioritize it
        if (targetFile != null) {
            files.stream()
                    .filter(f -> f.getPath().equals(targetFile))
                    .findFirst()
                    .ifPresent(f -> appendFileContent(context, f));
        }

        // Add important files first (layout, page, components)
        List<FileEntity> importantFiles = files.stream()
                .filter(f -> isImportantFile(f.getPath()))
                .limit(MAX_FILES_IN_CONTEXT)
                .collect(Collectors.toList());

        for (FileEntity file : importantFiles) {
            if (context.length() > MAX_CONTEXT_LENGTH)
                break;
            if (targetFile == null || !file.getPath().equals(targetFile)) {
                appendFileContent(context, file);
            }
        }

        // Add remaining files if space permits
        List<FileEntity> otherFiles = files.stream()
                .filter(f -> !isImportantFile(f.getPath()))
                .filter(f -> targetFile == null || !f.getPath().equals(targetFile))
                .limit(MAX_FILES_IN_CONTEXT / 2)
                .collect(Collectors.toList());

        for (FileEntity file : otherFiles) {
            if (context.length() > MAX_CONTEXT_LENGTH)
                break;
            appendFileContent(context, file);
        }

        return context.toString();
    }

    /**
     * Build context for a specific intent
     */
    public String buildContextForIntent(ProjectEntity project, PromptEntity.PromptIntent intent, String targetFile) {
        switch (intent) {
            case STYLE:
                return buildStyleContext(project);
            case FIX:
                return buildContext(project, targetFile);
            case REFACTOR:
                return buildContext(project, targetFile);
            default:
                return buildContext(project, targetFile);
        }
    }

    /**
     * Build context focused on styling
     */
    private String buildStyleContext(ProjectEntity project) {
        StringBuilder context = new StringBuilder();

        List<FileEntity> files = fileRepository.findByProjectIdOrderByPathAsc(project.getId());

        // Include CSS/styling files
        context.append("## Styling Files\n\n");
        files.stream()
                .filter(f -> f.getPath().endsWith(".css") ||
                        f.getPath().contains("tailwind") ||
                        f.getPath().contains("globals"))
                .forEach(f -> appendFileContent(context, f));

        // Include main layout/page files
        context.append("\n## Layout Files\n\n");
        files.stream()
                .filter(f -> f.getPath().contains("layout") || f.getPath().contains("page"))
                .limit(5)
                .forEach(f -> appendFileContent(context, f));

        return context.toString();
    }

    /**
     * Append file content to context
     */
    private void appendFileContent(StringBuilder context, FileEntity file) {
        if (file.getContent() == null || file.getContent().isEmpty())
            return;

        context.append("### ").append(file.getPath()).append("\n");
        context.append("```").append(getLanguageFromPath(file.getPath())).append("\n");
        context.append(file.getContent());
        if (!file.getContent().endsWith("\n")) {
            context.append("\n");
        }
        context.append("```\n\n");
    }

    /**
     * Check if a file is important for context
     */
    private boolean isImportantFile(String path) {
        return path.contains("layout") ||
                path.contains("page") ||
                path.endsWith("package.json") ||
                path.endsWith("tailwind.config") ||
                path.contains("/components/") ||
                path.contains("/lib/") ||
                path.endsWith("globals.css");
    }

    /**
     * Get language identifier from file path
     */
    private String getLanguageFromPath(String path) {
        if (path.endsWith(".tsx"))
            return "tsx";
        if (path.endsWith(".ts"))
            return "typescript";
        if (path.endsWith(".jsx"))
            return "jsx";
        if (path.endsWith(".js"))
            return "javascript";
        if (path.endsWith(".json"))
            return "json";
        if (path.endsWith(".css"))
            return "css";
        if (path.endsWith(".html"))
            return "html";
        if (path.endsWith(".md"))
            return "markdown";
        return "";
    }
}
