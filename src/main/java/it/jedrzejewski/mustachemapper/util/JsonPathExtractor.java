package it.jedrzejewski.mustachemapper.util;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility for extracting JSON fragments using JSONPath-like syntax
 */
public class JsonPathExtractor {
    
    /**
     * Extract JSON fragment by path
     * Supports: $.orders, $.user.profile, $.orders[0], $.orders[*]
     */
    public JsonNode extractPath(JsonNode rootNode, String jsonPath) {
        if (rootNode == null || jsonPath == null) {
            return null;
        }
        
        // Handle array wildcard specially
        if (jsonPath.endsWith("[*]")) {
            String arrayPath = jsonPath.substring(0, jsonPath.length() - 3);
            return extractSinglePath(rootNode, arrayPath);
        }
        
        return extractSinglePath(rootNode, jsonPath);
    }
    
    /**
     * Extract single path (non-wildcard)
     */
    private JsonNode extractSinglePath(JsonNode rootNode, String jsonPath) {
        String path = normalizeJsonPath(jsonPath);
        
        if (path.isEmpty()) {
            return rootNode;
        }
        
        String[] parts = path.split("\\.");
        JsonNode current = rootNode;
        
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
    private JsonNode processPathPart(JsonNode node, String part) {
        if (part.contains("[") && part.contains("]")) {
            return processArrayAccess(node, part);
        } else {
            return node.get(part);
        }
    }
    
    /**
     * Process array access like "orders[0]"
     */
    private JsonNode processArrayAccess(JsonNode node, String part) {
        String arrayField = part.substring(0, part.indexOf("["));
        String indexStr = part.substring(part.indexOf("[") + 1, part.indexOf("]"));
        
        // Get array node
        JsonNode arrayNode = arrayField.isEmpty() ? node : node.get(arrayField);
        
        if (arrayNode == null || !arrayNode.isArray()) {
            return null;
        }
        
        try {
            int index = Integer.parseInt(indexStr);
            if (index >= 0 && index < arrayNode.size()) {
                return arrayNode.get(index);
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
