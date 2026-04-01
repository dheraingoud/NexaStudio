package com.nexastudio.generator;

import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Dependency Manager Service.
 * Manages npm dependencies for projects.
 */
@Service
public class DependencyManager {

    // Core dependencies that should always be present
    private static final Map<String, String> CORE_DEPENDENCIES = Map.of(
            "next", "14.2.0",
            "react", "^18.2.0",
            "react-dom", "^18.2.0");

    // Common optional dependencies
    private static final Map<String, String> COMMON_DEPENDENCIES = Map.ofEntries(
            Map.entry("lucide-react", "^0.314.0"),
            Map.entry("clsx", "^2.1.0"),
            Map.entry("tailwind-merge", "^2.2.0"),
            Map.entry("class-variance-authority", "^0.7.0"),
            Map.entry("@radix-ui/react-slot", "^1.0.2"),
            Map.entry("@radix-ui/react-dialog", "^1.0.5"),
            Map.entry("@radix-ui/react-dropdown-menu", "^2.0.6"),
            Map.entry("@radix-ui/react-tabs", "^1.0.4"),
            Map.entry("@radix-ui/react-toast", "^1.1.5"),
            Map.entry("framer-motion", "^11.0.0"),
            Map.entry("zustand", "^4.5.0"),
            Map.entry("axios", "^1.6.0"),
            Map.entry("date-fns", "^3.3.0"),
            Map.entry("zod", "^3.22.0"),
            Map.entry("react-hook-form", "^7.50.0"),
            Map.entry("@hookform/resolvers", "^3.3.0"));

    /**
     * Get core dependencies
     */
    public Map<String, String> getCoreDependencies() {
        return new LinkedHashMap<>(CORE_DEPENDENCIES);
    }

    /**
     * Get dependency version by name
     */
    public String getDependencyVersion(String name) {
        if (CORE_DEPENDENCIES.containsKey(name)) {
            return CORE_DEPENDENCIES.get(name);
        }
        return COMMON_DEPENDENCIES.get(name);
    }

    /**
     * Check if dependency is valid
     */
    public boolean isValidDependency(String name) {
        return CORE_DEPENDENCIES.containsKey(name) ||
                COMMON_DEPENDENCIES.containsKey(name) ||
                isNpmPackage(name);
    }

    /**
     * Basic npm package name validation
     */
    private boolean isNpmPackage(String name) {
        if (name == null || name.isEmpty())
            return false;

        // Allow scoped packages (@org/package) and regular packages
        return name.matches("^(@[a-z0-9-~][a-z0-9-._~]*/)?[a-z0-9-~][a-z0-9-._~]*$");
    }

    /**
     * Generate package.json dependencies section
     */
    public String generateDependenciesJson(List<String> additionalDeps) {
        Map<String, String> deps = new LinkedHashMap<>(CORE_DEPENDENCIES);

        for (String dep : additionalDeps) {
            String version = getDependencyVersion(dep);
            if (version != null) {
                deps.put(dep, version);
            } else if (isNpmPackage(dep)) {
                deps.put(dep, "latest");
            }
        }

        StringBuilder json = new StringBuilder();
        json.append("{\n");

        List<String> entries = new ArrayList<>();
        for (Map.Entry<String, String> entry : deps.entrySet()) {
            entries.add(String.format("    \"%s\": \"%s\"", entry.getKey(), entry.getValue()));
        }

        json.append(String.join(",\n", entries));
        json.append("\n  }");

        return json.toString();
    }

    /**
     * Check for dependency conflicts
     */
    public List<String> checkConflicts(List<String> dependencies) {
        List<String> conflicts = new ArrayList<>();

        // Check for known conflicts
        if (dependencies.contains("react") && dependencies.contains("preact")) {
            conflicts.add("react and preact are conflicting");
        }

        return conflicts;
    }
}
