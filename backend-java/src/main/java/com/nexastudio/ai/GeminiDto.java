package com.nexastudio.ai;

import java.util.List;

/**
 * DTOs for Gemini API communication.
 * Structured according to Google's Generative AI API specification.
 */
public class GeminiDto {

    /**
     * Request body for Gemini API
     */
    public static class GeminiRequest {
        private List<Content> contents;
        private GenerationConfig generationConfig;
        private List<SafetySetting> safetySettings;
        private SystemInstruction systemInstruction;

        public GeminiRequest() {
        }

        public GeminiRequest(List<Content> contents, GenerationConfig generationConfig,
                List<SafetySetting> safetySettings, SystemInstruction systemInstruction) {
            this.contents = contents;
            this.generationConfig = generationConfig;
            this.safetySettings = safetySettings;
            this.systemInstruction = systemInstruction;
        }

        public List<Content> getContents() {
            return contents;
        }

        public void setContents(List<Content> contents) {
            this.contents = contents;
        }

        public GenerationConfig getGenerationConfig() {
            return generationConfig;
        }

        public void setGenerationConfig(GenerationConfig generationConfig) {
            this.generationConfig = generationConfig;
        }

        public List<SafetySetting> getSafetySettings() {
            return safetySettings;
        }

        public void setSafetySettings(List<SafetySetting> safetySettings) {
            this.safetySettings = safetySettings;
        }

        public SystemInstruction getSystemInstruction() {
            return systemInstruction;
        }

        public void setSystemInstruction(SystemInstruction systemInstruction) {
            this.systemInstruction = systemInstruction;
        }

        public static GeminiRequestBuilder builder() {
            return new GeminiRequestBuilder();
        }

        public static class GeminiRequestBuilder {
            private List<Content> contents;
            private GenerationConfig generationConfig;
            private List<SafetySetting> safetySettings;
            private SystemInstruction systemInstruction;

            public GeminiRequestBuilder contents(List<Content> contents) {
                this.contents = contents;
                return this;
            }

            public GeminiRequestBuilder generationConfig(GenerationConfig generationConfig) {
                this.generationConfig = generationConfig;
                return this;
            }

            public GeminiRequestBuilder safetySettings(List<SafetySetting> safetySettings) {
                this.safetySettings = safetySettings;
                return this;
            }

            public GeminiRequestBuilder systemInstruction(SystemInstruction systemInstruction) {
                this.systemInstruction = systemInstruction;
                return this;
            }

            public GeminiRequest build() {
                return new GeminiRequest(contents, generationConfig, safetySettings, systemInstruction);
            }
        }
    }

    public static class Content {
        private String role;
        private List<Part> parts;

        public Content() {
        }

        public Content(String role, List<Part> parts) {
            this.role = role;
            this.parts = parts;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }

        public static ContentBuilder builder() {
            return new ContentBuilder();
        }

        public static class ContentBuilder {
            private String role;
            private List<Part> parts;

            public ContentBuilder role(String role) {
                this.role = role;
                return this;
            }

            public ContentBuilder parts(List<Part> parts) {
                this.parts = parts;
                return this;
            }

            public Content build() {
                return new Content(role, parts);
            }
        }
    }

    public static class Part {
        private String text;

        public Part() {
        }

        public Part(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public static PartBuilder builder() {
            return new PartBuilder();
        }

        public static class PartBuilder {
            private String text;

            public PartBuilder text(String text) {
                this.text = text;
                return this;
            }

            public Part build() {
                return new Part(text);
            }
        }
    }

    public static class SystemInstruction {
        private List<Part> parts;

        public SystemInstruction() {
        }

        public SystemInstruction(List<Part> parts) {
            this.parts = parts;
        }

        public List<Part> getParts() {
            return parts;
        }

        public void setParts(List<Part> parts) {
            this.parts = parts;
        }

        public static SystemInstructionBuilder builder() {
            return new SystemInstructionBuilder();
        }

        public static class SystemInstructionBuilder {
            private List<Part> parts;

            public SystemInstructionBuilder parts(List<Part> parts) {
                this.parts = parts;
                return this;
            }

            public SystemInstruction build() {
                return new SystemInstruction(parts);
            }
        }
    }

    public static class GenerationConfig {
        private Double temperature;
        private Integer topK;
        private Double topP;
        private Integer maxOutputTokens;
        private String responseMimeType;

        public GenerationConfig() {
        }

        public GenerationConfig(Double temperature, Integer topK, Double topP,
                Integer maxOutputTokens, String responseMimeType) {
            this.temperature = temperature;
            this.topK = topK;
            this.topP = topP;
            this.maxOutputTokens = maxOutputTokens;
            this.responseMimeType = responseMimeType;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getTopK() {
            return topK;
        }

        public void setTopK(Integer topK) {
            this.topK = topK;
        }

        public Double getTopP() {
            return topP;
        }

        public void setTopP(Double topP) {
            this.topP = topP;
        }

        public Integer getMaxOutputTokens() {
            return maxOutputTokens;
        }

        public void setMaxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
        }

        public String getResponseMimeType() {
            return responseMimeType;
        }

        public void setResponseMimeType(String responseMimeType) {
            this.responseMimeType = responseMimeType;
        }

        public static GenerationConfigBuilder builder() {
            return new GenerationConfigBuilder();
        }

        public static class GenerationConfigBuilder {
            private Double temperature;
            private Integer topK;
            private Double topP;
            private Integer maxOutputTokens;
            private String responseMimeType;

            public GenerationConfigBuilder temperature(Double temperature) {
                this.temperature = temperature;
                return this;
            }

            public GenerationConfigBuilder topK(Integer topK) {
                this.topK = topK;
                return this;
            }

            public GenerationConfigBuilder topP(Double topP) {
                this.topP = topP;
                return this;
            }

            public GenerationConfigBuilder maxOutputTokens(Integer maxOutputTokens) {
                this.maxOutputTokens = maxOutputTokens;
                return this;
            }

            public GenerationConfigBuilder responseMimeType(String responseMimeType) {
                this.responseMimeType = responseMimeType;
                return this;
            }

            public GenerationConfig build() {
                return new GenerationConfig(temperature, topK, topP, maxOutputTokens, responseMimeType);
            }
        }
    }

    public static class SafetySetting {
        private String category;
        private String threshold;

        public SafetySetting() {
        }

        public SafetySetting(String category, String threshold) {
            this.category = category;
            this.threshold = threshold;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getThreshold() {
            return threshold;
        }

        public void setThreshold(String threshold) {
            this.threshold = threshold;
        }

        public static SafetySettingBuilder builder() {
            return new SafetySettingBuilder();
        }

        public static class SafetySettingBuilder {
            private String category;
            private String threshold;

            public SafetySettingBuilder category(String category) {
                this.category = category;
                return this;
            }

            public SafetySettingBuilder threshold(String threshold) {
                this.threshold = threshold;
                return this;
            }

            public SafetySetting build() {
                return new SafetySetting(category, threshold);
            }
        }
    }

    /**
     * Response from Gemini API
     */
    public static class GeminiResponse {
        private List<Candidate> candidates;
        private UsageMetadata usageMetadata;
        private PromptFeedback promptFeedback;

        public GeminiResponse() {
        }

        public GeminiResponse(List<Candidate> candidates, UsageMetadata usageMetadata, PromptFeedback promptFeedback) {
            this.candidates = candidates;
            this.usageMetadata = usageMetadata;
            this.promptFeedback = promptFeedback;
        }

        public List<Candidate> getCandidates() {
            return candidates;
        }

        public void setCandidates(List<Candidate> candidates) {
            this.candidates = candidates;
        }

        public UsageMetadata getUsageMetadata() {
            return usageMetadata;
        }

        public void setUsageMetadata(UsageMetadata usageMetadata) {
            this.usageMetadata = usageMetadata;
        }

        public PromptFeedback getPromptFeedback() {
            return promptFeedback;
        }

        public void setPromptFeedback(PromptFeedback promptFeedback) {
            this.promptFeedback = promptFeedback;
        }
    }

    public static class Candidate {
        private Content content;
        private String finishReason;
        private List<SafetyRating> safetyRatings;
        private Integer index;

        public Candidate() {
        }

        public Candidate(Content content, String finishReason, List<SafetyRating> safetyRatings, Integer index) {
            this.content = content;
            this.finishReason = finishReason;
            this.safetyRatings = safetyRatings;
            this.index = index;
        }

        public Content getContent() {
            return content;
        }

        public void setContent(Content content) {
            this.content = content;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }

        public List<SafetyRating> getSafetyRatings() {
            return safetyRatings;
        }

        public void setSafetyRatings(List<SafetyRating> safetyRatings) {
            this.safetyRatings = safetyRatings;
        }

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }
    }

    public static class SafetyRating {
        private String category;
        private String probability;

        public SafetyRating() {
        }

        public SafetyRating(String category, String probability) {
            this.category = category;
            this.probability = probability;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getProbability() {
            return probability;
        }

        public void setProbability(String probability) {
            this.probability = probability;
        }
    }

    public static class UsageMetadata {
        private Integer promptTokenCount;
        private Integer candidatesTokenCount;
        private Integer totalTokenCount;

        public UsageMetadata() {
        }

        public UsageMetadata(Integer promptTokenCount, Integer candidatesTokenCount, Integer totalTokenCount) {
            this.promptTokenCount = promptTokenCount;
            this.candidatesTokenCount = candidatesTokenCount;
            this.totalTokenCount = totalTokenCount;
        }

        public Integer getPromptTokenCount() {
            return promptTokenCount;
        }

        public void setPromptTokenCount(Integer promptTokenCount) {
            this.promptTokenCount = promptTokenCount;
        }

        public Integer getCandidatesTokenCount() {
            return candidatesTokenCount;
        }

        public void setCandidatesTokenCount(Integer candidatesTokenCount) {
            this.candidatesTokenCount = candidatesTokenCount;
        }

        public Integer getTotalTokenCount() {
            return totalTokenCount;
        }

        public void setTotalTokenCount(Integer totalTokenCount) {
            this.totalTokenCount = totalTokenCount;
        }
    }

    public static class PromptFeedback {
        private List<SafetyRating> safetyRatings;

        public PromptFeedback() {
        }

        public PromptFeedback(List<SafetyRating> safetyRatings) {
            this.safetyRatings = safetyRatings;
        }

        public List<SafetyRating> getSafetyRatings() {
            return safetyRatings;
        }

        public void setSafetyRatings(List<SafetyRating> safetyRatings) {
            this.safetyRatings = safetyRatings;
        }
    }

    /**
     * Structured output for code generation
     */
    public static class CodeGenerationOutput {
        private String explanation;
        private List<FileOperation> files;
        private List<String> dependencies;
        private String nextSteps;

        public CodeGenerationOutput() {
        }

        public CodeGenerationOutput(String explanation, List<FileOperation> files,
                List<String> dependencies, String nextSteps) {
            this.explanation = explanation;
            this.files = files;
            this.dependencies = dependencies;
            this.nextSteps = nextSteps;
        }

        public String getExplanation() {
            return explanation;
        }

        public void setExplanation(String explanation) {
            this.explanation = explanation;
        }

        public List<FileOperation> getFiles() {
            return files;
        }

        public void setFiles(List<FileOperation> files) {
            this.files = files;
        }

        public List<String> getDependencies() {
            return dependencies;
        }

        public void setDependencies(List<String> dependencies) {
            this.dependencies = dependencies;
        }

        public String getNextSteps() {
            return nextSteps;
        }

        public void setNextSteps(String nextSteps) {
            this.nextSteps = nextSteps;
        }

        public static CodeGenerationOutputBuilder builder() {
            return new CodeGenerationOutputBuilder();
        }

        public static class CodeGenerationOutputBuilder {
            private String explanation;
            private List<FileOperation> files;
            private List<String> dependencies;
            private String nextSteps;

            public CodeGenerationOutputBuilder explanation(String explanation) {
                this.explanation = explanation;
                return this;
            }

            public CodeGenerationOutputBuilder files(List<FileOperation> files) {
                this.files = files;
                return this;
            }

            public CodeGenerationOutputBuilder dependencies(List<String> dependencies) {
                this.dependencies = dependencies;
                return this;
            }

            public CodeGenerationOutputBuilder nextSteps(String nextSteps) {
                this.nextSteps = nextSteps;
                return this;
            }

            public CodeGenerationOutput build() {
                return new CodeGenerationOutput(explanation, files, dependencies, nextSteps);
            }
        }
    }

    public static class FileOperation {
        private String path;
        private String action; // CREATE, UPDATE, DELETE
        private String content;
        private String summary;

        public FileOperation() {
        }

        public FileOperation(String path, String action, String content, String summary) {
            this.path = path;
            this.action = action;
            this.content = content;
            this.summary = summary;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public static FileOperationBuilder builder() {
            return new FileOperationBuilder();
        }

        public static class FileOperationBuilder {
            private String path;
            private String action;
            private String content;
            private String summary;

            public FileOperationBuilder path(String path) {
                this.path = path;
                return this;
            }

            public FileOperationBuilder action(String action) {
                this.action = action;
                return this;
            }

            public FileOperationBuilder content(String content) {
                this.content = content;
                return this;
            }

            public FileOperationBuilder summary(String summary) {
                this.summary = summary;
                return this;
            }

            public FileOperation build() {
                return new FileOperation(path, action, content, summary);
            }
        }
    }
}
