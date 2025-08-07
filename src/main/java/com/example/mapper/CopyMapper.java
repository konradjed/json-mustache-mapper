package com.example.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Handles direct copying of JSON fragments without transformation
 */
public class CopyMapper {
    
    /**
     * Copy JSON fragment directly to target
     */
    public void processMapping(JsonNode extractedData, ObjectNode targetNode, String targetKey) {
        if (extractedData != null) {
            targetNode.set(targetKey, extractedData);
        }
    }
}
