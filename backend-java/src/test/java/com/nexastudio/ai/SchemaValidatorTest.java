package com.nexastudio.ai;

import com.nexastudio.ai.GeminiDto.CodeGenerationOutput;
import com.nexastudio.ai.GeminiDto.FileOperation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SchemaValidatorTest {

    @Autowired
    private SchemaValidator schemaValidator;

    @Test
    void shouldValidateCorrectOutput() {
        CodeGenerationOutput output = CodeGenerationOutput.builder()
                .explanation("Created a new component")
                .files(List.of(
                        FileOperation.builder()
                                .path("/components/Button.tsx")
                                .action("CREATE")
                                .content("export default function Button() { return <button>Click</button>; }")
                                .summary("Button component")
                                .build()
                ))
                .build();

        SchemaValidator.ValidationResult result = schemaValidator.validate(output);
        
        assertTrue(result.valid());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void shouldRejectProtectedPaths() {
        CodeGenerationOutput output = CodeGenerationOutput.builder()
                .explanation("Modifying node_modules")
                .files(List.of(
                        FileOperation.builder()
                                .path("/node_modules/react/index.js")
                                .action("UPDATE")
                                .content("malicious code")
                                .build()
                ))
                .build();

        SchemaValidator.ValidationResult result = schemaValidator.validate(output);
        
        // Protected paths are skipped (warned), not errored — result is still valid
        assertTrue(result.valid());
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("protected")));
        assertTrue(output.getFiles().isEmpty()); // File was removed from safe list
    }

    @Test
    void shouldRejectForbiddenPatterns() {
        CodeGenerationOutput output = CodeGenerationOutput.builder()
                .explanation("Component with eval")
                .files(List.of(
                        FileOperation.builder()
                                .path("/components/Evil.tsx")
                                .action("CREATE")
                                .content("export default function Evil() { eval('alert(1)'); return null; }")
                                .build()
                ))
                .build();

        SchemaValidator.ValidationResult result = schemaValidator.validate(output);
        
        assertFalse(result.valid());
        assertTrue(result.errors().stream().anyMatch(e -> e.contains("Forbidden")));
    }

    @Test
    void shouldRejectInvalidFileExtension() {
        CodeGenerationOutput output = CodeGenerationOutput.builder()
                .explanation("Executable file")
                .files(List.of(
                        FileOperation.builder()
                                .path("/malware.exe")
                                .action("CREATE")
                                .content("binary content")
                                .build()
                ))
                .build();

        SchemaValidator.ValidationResult result = schemaValidator.validate(output);
        
        // Invalid extensions are skipped (warned), not errored — result is still valid
        assertTrue(result.valid());
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("extension")));
        assertTrue(output.getFiles().isEmpty()); // File was removed from safe list
    }

    @Test
    void shouldWarnAboutMissingUseClient() {
        CodeGenerationOutput output = CodeGenerationOutput.builder()
                .explanation("Interactive component")
                .files(List.of(
                        FileOperation.builder()
                                .path("/components/Counter.tsx")
                                .action("CREATE")
                                .content("""
                                    import { useState } from 'react';
                                    export default function Counter() {
                                        const [count, setCount] = useState(0);
                                        return <button onClick={() => setCount(c => c+1)}>{count}</button>;
                                    }
                                    """)
                                .build()
                ))
                .build();

        SchemaValidator.ValidationResult result = schemaValidator.validate(output);
        
        assertTrue(result.valid()); // Valid but with warnings
        assertTrue(result.warnings().stream().anyMatch(w -> w.contains("use client")));
    }

    @Test
    void shouldSanitizeContent() {
        String content = "\uFEFFHello\r\nWorld\r";
        String sanitized = schemaValidator.sanitizeContent(content);
        
        assertFalse(sanitized.contains("\uFEFF"));
        assertFalse(sanitized.contains("\r"));
        assertTrue(sanitized.contains("Hello\nWorld"));
    }

    @Test
    void shouldNormalizePaths() {
        CodeGenerationOutput output = CodeGenerationOutput.builder()
                .files(List.of(
                        FileOperation.builder()
                                .path("components/Button.tsx") // Missing leading slash
                                .action("CREATE")
                                .content("export default function Button() { return <button>Click</button>; }")
                                .build()
                ))
                .build();

        SchemaValidator.ValidationResult result = schemaValidator.validate(output);
        
        assertTrue(result.valid());
        assertEquals("/components/Button.tsx", output.getFiles().get(0).getPath());
    }
}
