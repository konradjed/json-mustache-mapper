package com.example.mapper;

import com.example.config.MappingConfiguration.MappingRule;
import com.example.template.TemplateEngine;
import com.example.template.TemplateRegistry;
import com.example.util.JsonPathExtractor;
import com.example.wrapper.JsonNodeWrapper;
import com.example.wrapper.MultiSourceDataContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;

/**
 * Handles Mustache template processing for JSON data with support for multiple data sources
 */
public class MustacheMapper {
    
    private final ObjectMapper objectMapper;
    private final TemplateEngine templateEngine;
    private final JsonPathExtractor jsonPathExtractor;
    
    public MustacheMapper(ObjectMapper objectMapper, TemplateRegistry templateRegistry) {
        this.objectMapper = objectMapper;
        this.templateEngine = new TemplateEngine(templateRegistry);
        this.jsonPathExtractor = new JsonPathExtractor();
    }
    
    /**
     * Process JSON data through Mustache template with support for multiple data sources
     */
    public void processMapping(JsonNode sourceNode, ObjectNode targetNode, String targetKey, MappingRule rule) throws IOException {
        String templateName = rule.getTemplateName();
        if (templateName == null) {
            throw new IllegalArgumentException("Template name is required for MUSTACHE mapping");
        }
        
        if (rule.hasMultipleSources()) {
            processMultiSourceMapping(sourceNode, targetNode, targetKey, rule);
        } else {
            // Single source processing (backward compatibility)
            JsonNode extractedData = jsonPathExtractor.extractPath(sourceNode, rule.getJsonPath());
            if (extractedData == null) {
                return;
            }
            
            if (rule.isArrayProcessing() && extractedData.isArray()) {
                processArrayMapping(extractedData, targetNode, targetKey, templateName);
            } else {
                processSingleMapping(extractedData, targetNode, targetKey, templateName);
            }
        }
    }
    
    /**
     * Process mapping with multiple data sources
     */
    private void processMultiSourceMapping(JsonNode sourceNode, ObjectNode targetNode, String targetKey, MappingRule rule) {
        List<String> jsonPaths = rule.getJsonPaths();
        String templateName = rule.getTemplateName();
        
        // Determine if we're dealing with array processing based on the first path
        boolean isArrayProcessing = rule.isArrayProcessing();
        
        if (isArrayProcessing) {
            processMultiSourceArrayMapping(sourceNode, targetNode, targetKey, templateName, jsonPaths);
        } else {
            processMultiSourceSingleMapping(sourceNode, targetNode, targetKey, templateName, jsonPaths);
        }
    }
    
    /**
     * Process array items with multiple data sources
     */
    private void processMultiSourceArrayMapping(JsonNode sourceNode, ObjectNode targetNode, String targetKey, 
                                              String templateName, List<String> jsonPaths) {
        ArrayNode targetArray = objectMapper.createArrayNode();
        
        // Extract the primary array data (first JSONPath)
        String primaryPath = jsonPaths.get(0);
        JsonNode primaryArrayData = jsonPathExtractor.extractPath(sourceNode, primaryPath);
        
        if (primaryArrayData != null && primaryArrayData.isArray()) {
            // For each item in the primary array, create a context with all data sources
            for (JsonNode arrayItem : primaryArrayData) {
                MultiSourceDataContext context = createMultiSourceContext(sourceNode, jsonPaths, arrayItem);
                String renderedText = templateEngine.render(templateName, context);
                targetArray.add(renderedText);
            }
        }
        
        targetNode.set(targetKey, targetArray);
    }
    
    /**
     * Process single item with multiple data sources
     */
    private void processMultiSourceSingleMapping(JsonNode sourceNode, ObjectNode targetNode, String targetKey, 
                                               String templateName, List<String> jsonPaths) {
        MultiSourceDataContext context = createMultiSourceContext(sourceNode, jsonPaths, null);
        String renderedText = templateEngine.render(templateName, context);
        targetNode.put(targetKey, renderedText);
    }
    
    /**
     * Create a multi-source data context for template rendering
     */
    private MultiSourceDataContext createMultiSourceContext(JsonNode sourceNode, List<String> jsonPaths, JsonNode primaryItem) {
        MultiSourceDataContext context = new MultiSourceDataContext();
        
        for (int i = 0; i < jsonPaths.size(); i++) {
            String jsonPath = jsonPaths.get(i);
            JsonNode extractedData;
            
            if (i == 0 && primaryItem != null) {
                // For the first path in array processing, use the current array item
                extractedData = primaryItem;
            } else {
                // For other paths, extract from the source node
                extractedData = jsonPathExtractor.extractPath(sourceNode, jsonPath);
            }
            
            if (extractedData != null) {
                if (i == 0) {
                    // First data source becomes the primary context
                    context.setPrimaryDataSource(extractedData);
                } else {
                    // Additional data sources are accessible with generated keys
                    String sourceKey = "source" + (i + 1);
                    context.addDataSource(sourceKey, extractedData);
                }
            }
        }
        
        return context;
    }
    
    /**
     * Process array of items through template (single source - backward compatibility)
     */
    private void processArrayMapping(JsonNode arrayData, ObjectNode targetNode, String targetKey, String templateName) {
        ArrayNode targetArray = objectMapper.createArrayNode();
        
        for (JsonNode item : arrayData) {
            JsonNodeWrapper wrapper = new JsonNodeWrapper(item);
            String renderedText = templateEngine.render(templateName, wrapper);
            targetArray.add(renderedText);
        }
        
        targetNode.set(targetKey, targetArray);
    }
    
    /**
     * Process single item through template (single source - backward compatibility)
     */
    private void processSingleMapping(JsonNode singleData, ObjectNode targetNode, String targetKey, String templateName) {
        JsonNodeWrapper wrapper = new JsonNodeWrapper(singleData);
        String renderedText = templateEngine.render(templateName, wrapper);
        targetNode.put(targetKey, renderedText);
    }
}
