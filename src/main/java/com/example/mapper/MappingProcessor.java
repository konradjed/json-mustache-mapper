package com.example.mapper;

import com.example.config.MappingConfiguration;
import com.example.template.TemplateRegistry;
import com.example.util.JsonPathExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;

/**
 * Processes mapping configurations and applies transformations to JSON nodes
 */
public class MappingProcessor {
    
    private final ObjectMapper objectMapper;
    private final JsonPathExtractor jsonPathExtractor;
    private final MustacheMapper mustacheMapper;
    private final CopyMapper copyMapper;
    
    public MappingProcessor(ObjectMapper objectMapper, TemplateRegistry templateRegistry) {
        this.objectMapper = objectMapper;
        this.jsonPathExtractor = new JsonPathExtractor();
        this.mustacheMapper = new MustacheMapper(objectMapper, templateRegistry);
        this.copyMapper = new CopyMapper();
    }
    
    /**
     * Process the entire mapping configuration
     */
    @SuppressWarnings("unchecked")
    public void processMapping(JsonNode sourceNode, ObjectNode targetNode, MappingConfiguration config) throws IOException {
        for (Map.Entry<String, Object> entry : config.getConfigMap().entrySet()) {
            String targetKey = entry.getKey();
            Object configValue = entry.getValue();
            
            if (config.isMappingRule(configValue)) {
                processMappingRule(sourceNode, targetNode, targetKey, config.getMappingRule(configValue));
            } else if (configValue instanceof Map) {
                // Nested configuration
                ObjectNode nestedTarget = objectMapper.createObjectNode();
                MappingConfiguration nestedConfig = new MappingConfiguration((Map<String, Object>) configValue);
                processMapping(sourceNode, nestedTarget, nestedConfig);
                targetNode.set(targetKey, nestedTarget);
            }
        }
    }
    
    /**
     * Process a single mapping rule
     */
    private void processMappingRule(JsonNode sourceNode, ObjectNode targetNode, String targetKey, 
                                   MappingConfiguration.MappingRule rule) throws IOException {
        
        switch (rule.getMapperType()) {
            case MUSTACHE:
                mustacheMapper.processMapping(sourceNode, targetNode, targetKey, rule);
                break;
            case COPY:
                JsonNode extractedData = jsonPathExtractor.extractPath(sourceNode, rule.getJsonPath());
                copyMapper.processMapping(extractedData, targetNode, targetKey);
                break;
            case TRANSFORM:
                // Reserved for future custom transformations
                throw new UnsupportedOperationException("TRANSFORM mapper not implemented yet");
        }
    }
}
