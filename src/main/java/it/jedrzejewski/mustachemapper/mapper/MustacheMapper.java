package it.jedrzejewski.mustachemapper.mapper;

import it.jedrzejewski.mustachemapper.config.MappingConfiguration.MappingRule;
import it.jedrzejewski.mustachemapper.template.TemplateEngine;
import it.jedrzejewski.mustachemapper.template.TemplateRegistry;
import it.jedrzejewski.mustachemapper.util.MapPathExtractor;
import it.jedrzejewski.mustachemapper.wrapper.MultiSourceDataContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Handles Mustache template processing with support for single and multiple data sources
 */
public class MustacheMapper {

    public static final String VALUE = "value";
    private final TemplateEngine templateEngine;
    private final MapPathExtractor pathExtractor;
    
    public MustacheMapper(TemplateRegistry templateRegistry) {
        this.templateEngine = new TemplateEngine(templateRegistry);
        this.pathExtractor = new MapPathExtractor();
    }
    
    /**
     * Main entry point for processing mapping rules
     */
    public void processMapping(Map<String, Object> sourceData, Map<String, Object> targetData, String targetKey, MappingRule rule) {
        validateRule(rule);
        
        MappingRequest request = new MappingRequest(sourceData, targetData, targetKey, rule);
        
        if (rule.hasMultipleSources()) {
            processMultiSourceMapping(request);
        } else {
            processSingleSourceMapping(request);
        }
    }
    
    // ========== Single Source Processing ==========
    
    private void processSingleSourceMapping(MappingRequest request) {
        Object extractedData = pathExtractor.extractPath(request.sourceData, request.rule.getJsonPath());
        if (extractedData == null) {
            return;
        }
        
        if (request.rule.isArrayProcessing() && extractedData instanceof List) {
            processArrayData((List<?>) extractedData, request);
        } else {
            processSingleData(extractedData, request);
        }
    }
    
    private void processArrayData(List<?> arrayData, MappingRequest request) {
        List<String> results = new ArrayList<>();
        
        for (Object item : arrayData) {
            Map<String, Object> context = convertToMap(item);
            String rendered = templateEngine.render(request.rule.getTemplateName(), context);
            results.add(rendered);
        }
        
        request.targetData.put(request.targetKey, results);
    }
    
    private void processSingleData(Object data, MappingRequest request) {
        Map<String, Object> context = convertToMap(data);
        String rendered = templateEngine.render(request.rule.getTemplateName(), context);
        request.targetData.put(request.targetKey, rendered);
    }
    
    // ========== Multi Source Processing ==========
    
    private void processMultiSourceMapping(MappingRequest request) {
        if (request.rule.isArrayProcessing()) {
            processMultiSourceArray(request);
        } else {
            processMultiSourceSingle(request);
        }
    }
    
    private void processMultiSourceArray(MappingRequest request) {
        List<String> jsonPaths = request.rule.getJsonPaths();
        Object primaryArrayData = pathExtractor.extractPath(request.sourceData, jsonPaths.get(0));
        
        if (!(primaryArrayData instanceof List)) {
            return;
        }
        
        List<String> results = new ArrayList<>();
        List<?> arrayList = (List<?>) primaryArrayData;
        
        for (Object arrayItem : arrayList) {
            MultiSourceDataContext context = createMultiSourceContext(request.sourceData, jsonPaths, arrayItem);
            String rendered = templateEngine.render(request.rule.getTemplateName(), context);
            results.add(rendered);
        }
        
        request.targetData.put(request.targetKey, results);
    }
    
    private void processMultiSourceSingle(MappingRequest request) {
        MultiSourceDataContext context = createMultiSourceContext(request.sourceData, request.rule.getJsonPaths(), null);
        String rendered = templateEngine.render(request.rule.getTemplateName(), context);
        request.targetData.put(request.targetKey, rendered);
    }
    
    // ========== Context Creation ==========
    
    private MultiSourceDataContext createMultiSourceContext(Map<String, Object> sourceData, List<String> jsonPaths, Object primaryItem) {
        MultiSourceDataContext context = new MultiSourceDataContext();
        
        for (int i = 0; i < jsonPaths.size(); i++) {
            Object extractedData = extractDataForSource(sourceData, jsonPaths.get(i), i == 0 ? primaryItem : null);
            
            if (extractedData != null) {
                addDataToContext(context, extractedData, i);
            }
        }
        
        return context;
    }
    
    private Object extractDataForSource(Map<String, Object> sourceData, String jsonPath, Object primaryItem) {
        return primaryItem != null ? primaryItem : pathExtractor.extractPath(sourceData, jsonPath);
    }
    
    private void addDataToContext(MultiSourceDataContext context, Object data, int sourceIndex) {
        if (sourceIndex == 0) {
            addPrimaryData(context, data);
        } else {
            addSecondaryData(context, data, sourceIndex);
        }
    }
    
    private void addPrimaryData(MultiSourceDataContext context, Object data) {
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapData = (Map<String, Object>) data;
            context.setPrimaryDataSource(mapData);
        } else {
            context.addValue(VALUE, data);
        }
    }
    
    private void addSecondaryData(MultiSourceDataContext context, Object data, int sourceIndex) {
        String sourceKey = "source" + (sourceIndex + 1);
        
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapData = (Map<String, Object>) data;
            context.addDataSource(sourceKey, mapData);
        } else {
            Map<String, Object> wrappedData = Map.of(VALUE, data);
            context.addDataSource(sourceKey, wrappedData);
        }
    }
    
    // ========== Utility Methods ==========
    
    private void validateRule(MappingRule rule) {
        if (rule.getTemplateName() == null) {
            throw new IllegalArgumentException("Template name is required for MUSTACHE mapping");
        }
    }
    
    private Map<String, Object> convertToMap(Object data) {
        if (data instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> mapData = (Map<String, Object>) data;
            return mapData;
        }
        return Map.of(VALUE, data);
    }
    
    // ========== Inner Class for Request Data ==========
    
    /**
     * Encapsulates mapping request data to reduce parameter passing
     */
    private static class MappingRequest {
        final Map<String, Object> sourceData;
        final Map<String, Object> targetData;
        final String targetKey;
        final MappingRule rule;
        
        MappingRequest(Map<String, Object> sourceData, Map<String, Object> targetData, String targetKey, MappingRule rule) {
            this.sourceData = sourceData;
            this.targetData = targetData;
            this.targetKey = targetKey;
            this.rule = rule;
        }
    }
}