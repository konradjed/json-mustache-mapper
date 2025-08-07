package com.example.mapper;

import com.example.config.MappingConfiguration.MappingRule;
import com.example.template.TemplateEngine;
import com.example.template.TemplateRegistry;
import com.example.wrapper.JsonNodeWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * Handles Mustache template processing for JSON data
 */
public class MustacheMapper {
    
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    
    public MustacheMapper(ObjectMapper objectMapper, TemplateRegistry templateRegistry) {
        this.objectMapper = objectMapper;
        this.templateEngine = new TemplateEngine(templateRegistry);
    }
    
    /**
     * Process JSON data through Mustache template
     */
    public void processMapping(JsonNode extractedData, ObjectNode targetNode, String targetKey, MappingRule rule) throws IOException {
        if (extractedData == null) {
            return;
        }
        
        String templateName = rule.getTemplateName();
        if (templateName == null) {
            throw new IllegalArgumentException("Template name is required for MUSTACHE mapping");
        }
        
        if (rule.isArrayProcessing() && extractedData.isArray()) {
            processArrayMapping(extractedData, targetNode, targetKey, templateName);
        } else {
            processSingleMapping(extractedData, targetNode, targetKey, templateName);
        }
    }
    
    /**
     * Process array of items through template
     */
    private void processArrayMapping(JsonNode arrayData, ObjectNode targetNode, String targetKey, String templateName) throws IOException {
        ArrayNode targetArray = objectMapper.createArrayNode();
        
        for (JsonNode item : arrayData) {
            JsonNodeWrapper wrapper = new JsonNodeWrapper(item);
            String renderedText = templateEngine.render(templateName, wrapper);
            targetArray.add(renderedText);
        }
        
        targetNode.set(targetKey, targetArray);
    }
    
    /**
     * Process single item through template
     */
    private void processSingleMapping(JsonNode singleData, ObjectNode targetNode, String targetKey, String templateName) throws IOException {
        JsonNodeWrapper wrapper = new JsonNodeWrapper(singleData);
        String renderedText = templateEngine.render(templateName, wrapper);
        targetNode.put(targetKey, renderedText);
    }
}
