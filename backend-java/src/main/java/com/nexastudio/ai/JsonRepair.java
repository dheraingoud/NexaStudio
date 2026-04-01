package com.nexastudio.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility for repairing common JSON issues from AI model outputs.
 * AI models sometimes produce malformed JSON when embedding code content
 * that contains unescaped characters, template literals, or control characters.
 */
public final class JsonRepair {

    private static final Logger log = LoggerFactory.getLogger(JsonRepair.class);

    /**
     * A lenient ObjectMapper that tolerates common JSON formatting issues
     * from AI models: unescaped control chars, trailing commas, comments, etc.
     */
    public static final ObjectMapper LENIENT_MAPPER = JsonMapper.builder()
            .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
            .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)
            .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)
            .enable(JsonReadFeature.ALLOW_JAVA_COMMENTS)
            .enable(JsonReadFeature.ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS)
            .enable(JsonReadFeature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER)
            .build();

    private JsonRepair() {}

    /**
     * Attempt to parse JSON, first with strict mapper, then with lenient mapper,
     * and finally with text repair + lenient mapper.
     */
    public static <T> T parse(String json, Class<T> clazz, ObjectMapper strictMapper) throws JsonProcessingException {
        // 1. Try strict parse first (fastest path)
        try {
            return strictMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.debug("Strict JSON parse failed, trying lenient: {}", e.getMessage());
        }

        // 2. Try lenient parse
        try {
            return LENIENT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.debug("Lenient JSON parse failed, trying text repair: {}", e.getMessage());
        }

        // 3. Try text repair + lenient parse
        String repaired = repairJsonText(json);
        return LENIENT_MAPPER.readValue(repaired, clazz);
    }

    /**
     * Repair common JSON text issues from AI outputs.
     * This handles:
     * - Unescaped newlines/tabs within string values
     * - Unescaped backslashes
     * - Unescaped double-quotes inside string values (e.g. regex [^"'] in code)
     * - Truncated trailing content (unclosed strings/arrays/objects)
     */
    static String repairJsonText(String text) {
        if (text == null) return null;

        StringBuilder sb = new StringBuilder(text.length() + 256);
        boolean inString = false;
        boolean escaped = false;

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (escaped) {
                // Sanitize invalid JSON escape sequences from AI output
                if (c == 'n' || c == 'r' || c == 't' || c == 'b' || c == 'f'
                        || c == '"' || c == '\\' || c == '/' || c == 'u') {
                    // Valid JSON escape — keep as-is
                    sb.append(c);
                } else {
                    // Invalid escape (e.g. `\ `, `\w`, `\d`) — double the backslash
                    // so Jackson sees a literal backslash followed by the character
                    sb.append('\\');
                    sb.append(c);
                }
                escaped = false;
                continue;
            }

            if (c == '\\' && inString) {
                escaped = true;
                sb.append(c);
                continue;
            }

            if (c == '"') {
                if (!inString) {
                    // Opening a new string
                    inString = true;
                    sb.append(c);
                } else {
                    // Potential end of string — peek ahead to decide
                    if (isLegitimateStringEnd(text, i)) {
                        inString = false;
                        sb.append(c);
                    } else {
                        // Unescaped quote inside string value — escape it
                        sb.append("\\\"");
                        log.trace("Escaped rogue quote at position {}", i);
                    }
                }
                continue;
            }

            if (inString) {
                // Escape literal control characters inside strings
                switch (c) {
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\r':
                        sb.append("\\r");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\b':
                        sb.append("\\b");
                        break;
                    case '\f':
                        sb.append("\\f");
                        break;
                    default:
                        if (c < 0x20) {
                            sb.append(String.format("\\u%04x", (int) c));
                        } else {
                            sb.append(c);
                        }
                        break;
                }
            } else {
                sb.append(c);
            }
        }

        String result = sb.toString();

        // If JSON is truncated (unclosed), try to close it
        if (inString) {
            result = closeTruncatedJson(result);
        }

        return result;
    }

    /**
     * Determines whether a `"` at position {@code i} inside a string is a
     * legitimate string terminator.  We peek ahead (skipping whitespace) and
     * check whether the next meaningful character is valid JSON structure
     * (`,`, `}`, `]`, `:`) which would follow a properly closed string.
     * If not, the quote is likely embedded content (e.g. regex, HTML attributes)
     * and should be escaped instead of closing the string.
     */
    private static boolean isLegitimateStringEnd(String text, int i) {
        int ahead = i + 1;
        // skip whitespace / newlines
        while (ahead < text.length()) {
            char n = text.charAt(ahead);
            if (n != ' ' && n != '\t' && n != '\n' && n != '\r') break;
            ahead++;
        }
        if (ahead >= text.length()) return true; // end-of-input → close string

        char next = text.charAt(ahead);
        // Valid JSON tokens after a complete string value
        return next == ',' || next == '}' || next == ']' || next == ':'
                // Could also be the start of the next key/value pair
                || next == '"';
    }

    /**
     * Attempt to close truncated JSON by finding the last valid structure
     * and closing any open strings, arrays, or objects.
     */
    private static String closeTruncatedJson(String json) {
        // Find the last complete file entry, close string and structures
        int lastCompleteEntry = json.lastIndexOf("},");
        if (lastCompleteEntry > 0) {
            // Truncate to last complete entry + close the array and root object
            String truncated = json.substring(0, lastCompleteEntry + 1);
            // Count open braces/brackets to determine closing
            int openBraces = 0, openBrackets = 0;
            boolean inStr = false;
            boolean esc = false;
            for (char c : truncated.toCharArray()) {
                if (esc) { esc = false; continue; }
                if (c == '\\' && inStr) { esc = true; continue; }
                if (c == '"') { inStr = !inStr; continue; }
                if (!inStr) {
                    if (c == '{') openBraces++;
                    else if (c == '}') openBraces--;
                    else if (c == '[') openBrackets++;
                    else if (c == ']') openBrackets--;
                }
            }
            StringBuilder closer = new StringBuilder(truncated);
            for (int i = 0; i < openBrackets; i++) closer.append(']');
            for (int i = 0; i < openBraces; i++) closer.append('}');
            log.warn("Repaired truncated JSON — dropped content after last complete entry");
            return closer.toString();
        }
        return json;
    }
}
