package com.example;

import com.example.config.MappingConfiguration;
import com.example.mapper.MappingProcessor;
import com.example.template.TemplateRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Map;

/**
 * Main facade for JSON structure mapping operations.
 * Orchestrates the mapping process from source JSON to target JSON structure.
 */
public class JsonStructureMapper {
    
    private final ObjectMapper objectMapper;
    private final TemplateRegistry templateRegistry;
    private final MappingProcessor mappingProcessor;
    
    public JsonStructureMapper() {
        this.objectMapper = new ObjectMapper();
        this.templateRegistry = new TemplateRegistry();
        this.mappingProcessor = new MappingProcessor(objectMapper, templateRegistry);
    }
    
    /**
     * Transform source JSON to target structure using mapping configuration
     * 
     * @param sourceJson Source JSON string
     * @param mappingConfig Configuration defining the target structure
     * @return Transformed JSON string
     */
    public String transformJsonStructure(String sourceJson, Map<String, Object> mappingConfig) throws IOException {
        JsonNode sourceNode = objectMapper.readTree(sourceJson);
        ObjectNode targetNode = objectMapper.createObjectNode();
        
        MappingConfiguration config = new MappingConfiguration(mappingConfig);
        mappingProcessor.processMapping(sourceNode, targetNode, config);
        
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(targetNode);
    }
    
    /**
     * Add a custom template to the registry
     */
    public void registerTemplate(String templateName, String templateContent) {
        templateRegistry.addTemplate(templateName, templateContent);
    }
    
    /**
     * Get the template registry for advanced operations
     */
    public TemplateRegistry getTemplateRegistry() {
        return templateRegistry;
    }
}
