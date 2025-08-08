package it.jedrzejewski.mustachemapper.util;

import java.util.List;
import java.util.Map;

/**
 * Utility for extracting data fragments from Map using JSONPath-like syntax
 */
public class MapPathExtractor {
    
    /**
     * Extract data fragment by path
     * Supports: $.orders, $.user.profile, $.orders[0], $.orders[*]
     */
    public Object extractPath(Map<String, Object> rootData, String jsonPath) {
        if (rootData == null || jsonPath == null) {
            return null;
        }
        
        // Handle array wildcard specially
        if (jsonPath.endsWith("[*]")) {
            String arrayPath = jsonPath.substring(0, jsonPath.length() - 3);
            return extractSinglePath(rootData, arrayPath);
        }
        
        return extractSinglePath(rootData, jsonPath);
    }
    
    /**
     * Extract single path (non-wildcard)
     */
    private Object extractSinglePath(Map<String, Object> rootData, String jsonPath) {
        String path = normalizeJsonPath(jsonPath);
        
        if (path.isEmpty()) {
            return rootData;
        }
        
        String[] parts = path.split("\\.");
        Object current = rootData;
        
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            
            current = processPathPart(current, part);
        }
        
        return current;
    }
    
    /**
     * Process a single path part (field access or array index)
     */
    private Object processPathPart(Object data, String part) {
        if (part.contains("[") && part.contains("]")) {
            return processArrayAccess(data, part);
        } else {
            if (data instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) data;
                return map.get(part);
            }
            return null;
        }
    }
    
    /**
     * Process array access like "orders[0]"
     */
    private Object processArrayAccess(Object data, String part) {
        String arrayField = part.substring(0, part.indexOf("["));
        String indexStr = part.substring(part.indexOf("[") + 1, part.indexOf("]"));
        
        // Get array data
        Object arrayData;
        if (arrayField.isEmpty()) {
            arrayData = data;
        } else if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) data;
            arrayData = map.get(arrayField);
        } else {
            return null;
        }
        
        if (!(arrayData instanceof List)) {
            return null;
        }
        
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) arrayData;
        
        try {
            int index = Integer.parseInt(indexStr);
            if (index >= 0 && index < list.size()) {
                return list.get(index);
            }
        } catch (NumberFormatException e) {
            // Invalid index format
        }
        
        return null;
    }
    
    /**
     * Normalize JSONPath (remove leading $. or $)
     */
    private String normalizeJsonPath(String jsonPath) {
        if (jsonPath.startsWith("$.")) {
            return jsonPath.substring(2);
        } else if (jsonPath.startsWith("$")) {
            return jsonPath.substring(1);
        }
        return jsonPath;
    }
}