package it.jedrzejewski.mustachemapper.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.*;

/**
 * Represents a mapping configuration that defines how to transform
 * source JSON structure to target JSON structure.
 */
@Data
@RequiredArgsConstructor
public class MappingConfiguration {
    
    private final Map<String, Object> configMap;

    /**
     * Check if a configuration entry is a mapping rule
     */
    @SuppressWarnings("unchecked")
    public boolean isMappingRule(Object config) {
        return config instanceof Map && ((Map<String, Object>) config).containsKey("mapperType");
    }
    
    /**
     * Extract mapping rule from configuration
     */
    @SuppressWarnings("unchecked")
    public MappingRule getMappingRule(Object config) {
        if (!isMappingRule(config)) {
            throw new IllegalArgumentException("Configuration is not a mapping rule");
        }
        
        Map<String, Object> ruleMap = (Map<String, Object>) config;
        
        // Handle both single jsonPath (string) and multiple jsonPath (array)
        List<String> jsonPaths = extractJsonPaths(ruleMap);
        
        return new MappingRule(
            MapperType.valueOf(((String) ruleMap.get("mapperType")).toUpperCase()),
            (String) ruleMap.get("templateName"),
            jsonPaths
        );
    }
    
    /**
     * Extract JSONPath expressions from configuration, handling both single and multiple paths
     */
    private List<String> extractJsonPaths(Map<String, Object> ruleMap) {
        Object jsonPathValue = ruleMap.get("jsonPath");
        
        if (jsonPathValue == null) {
            return Collections.emptyList();
        }
        
        if (jsonPathValue instanceof String jsonPath) {
            // Single JSONPath (backward compatibility)
            return List.of(jsonPath);
        } else if (jsonPathValue instanceof List<?> pathList) {
            // Multiple JSONPaths
            return pathList.stream()
                    .map(String::valueOf)
                    .toList();
        } else {
            throw new IllegalArgumentException("jsonPath must be either a string or an array of strings");
        }
    }
    
    /**
     * Represents a single mapping rule with support for multiple data sources
     */
    @Data
    public static class MappingRule {
        private final MapperType mapperType;
        private final String templateName;
        private final List<String> jsonPaths;
        
        public MappingRule(MapperType mapperType, String templateName, List<String> jsonPaths) {
            this.mapperType = mapperType;
            this.templateName = templateName;
            this.jsonPaths = jsonPaths != null ? jsonPaths : Collections.emptyList();
        }
        
        // Backward compatibility constructor
        public MappingRule(MapperType mapperType, String templateName, String jsonPath) {
            this(mapperType, templateName, 
                 jsonPath != null ? List.of(jsonPath) : Collections.emptyList());
        }

        // Backward compatibility method
        public String getJsonPath() {
            return jsonPaths.isEmpty() ? null : jsonPaths.get(0);
        }
        
        public boolean hasMultipleSources() {
            return jsonPaths.size() > 1;
        }
        
        public boolean isArrayProcessing() {
            return !jsonPaths.isEmpty() && jsonPaths.get(0).endsWith("[*]");
        }
        
        public String getArrayPath() {
            if (!isArrayProcessing()) {
                throw new IllegalStateException("Not an array processing rule");
            }
            String firstPath = jsonPaths.get(0);
            return firstPath.substring(0, firstPath.length() - 3);
        }
    }
}
