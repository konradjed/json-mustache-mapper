package com.example.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a mapping configuration that defines how to transform
 * source JSON structure to target JSON structure.
 */
public class MappingConfiguration {
    
    private final Map<String, Object> configMap;
    
    public MappingConfiguration(Map<String, Object> configMap) {
        this.configMap = configMap;
    }
    
    public Map<String, Object> getConfigMap() {
        return configMap;
    }
    
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
    @SuppressWarnings("unchecked")
    private List<String> extractJsonPaths(Map<String, Object> ruleMap) {
        Object jsonPathValue = ruleMap.get("jsonPath");
        
        if (jsonPathValue == null) {
            return Collections.emptyList();
        }
        
        if (jsonPathValue instanceof String) {
            // Single JSONPath (backward compatibility)
            return Arrays.asList((String) jsonPathValue);
        } else if (jsonPathValue instanceof List) {
            // Multiple JSONPaths
            List<?> pathList = (List<?>) jsonPathValue;
            return pathList.stream()
                    .map(String::valueOf)
                    .collect(java.util.stream.Collectors.toList());
        } else {
            throw new IllegalArgumentException("jsonPath must be either a string or an array of strings");
        }
    }
    
    /**
     * Represents a single mapping rule
     */
    public static class MappingRule {
        private final MapperType mapperType;
        private final String templateName;
        private final String jsonPath;
        
        public MappingRule(MapperType mapperType, String templateName, String jsonPath) {
            this.mapperType = mapperType;
            this.templateName = templateName;
            this.jsonPath = jsonPath;
        }
        
        public MapperType getMapperType() { return mapperType; }
        public String getTemplateName() { return templateName; }
        public String getJsonPath() { return jsonPath; }
        
        public boolean isArrayProcessing() {
            return jsonPath != null && jsonPath.endsWith("[*]");
        }
        
        public String getArrayPath() {
            if (!isArrayProcessing()) {
                throw new IllegalStateException("Not an array processing rule");
            }
            return jsonPath.substring(0, jsonPath.length() - 3);
        }
    }
}
