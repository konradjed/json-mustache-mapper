package it.jedrzejewski.mustachemapper.mapper;

import it.jedrzejewski.mustachemapper.config.MappingConfiguration;
import it.jedrzejewski.mustachemapper.template.TemplateRegistry;
import it.jedrzejewski.mustachemapper.util.MapPathExtractor;

import java.util.HashMap;
import java.util.Map;

/**
 * Processes mapping configurations and applies transformations to Map data
 */
public class MappingProcessor {

    private final MapPathExtractor mapPathExtractor;
    private final MustacheMapper mustacheMapper;
    private final CopyMapper copyMapper;
    
    public MappingProcessor(TemplateRegistry templateRegistry) {
        this.mapPathExtractor = new MapPathExtractor();
        this.mustacheMapper = new MustacheMapper(templateRegistry);
        this.copyMapper = new CopyMapper();
    }
    
    /**
     * Process the entire mapping configuration
     */
    @SuppressWarnings("unchecked")
    public void processMapping(Map<String, Object> sourceData, Map<String, Object> targetData, MappingConfiguration config) {
        for (Map.Entry<String, Object> entry : config.getConfigMap().entrySet()) {
            String targetKey = entry.getKey();
            Object configValue = entry.getValue();
            
            if (config.isMappingRule(configValue)) {
                processMappingRule(sourceData, targetData, targetKey, config.getMappingRule(configValue));
            } else if (configValue instanceof Map) {
                // Nested configuration
                Map<String, Object> nestedTarget = new HashMap<>();
                MappingConfiguration nestedConfig = new MappingConfiguration((Map<String, Object>) configValue);
                processMapping(sourceData, nestedTarget, nestedConfig);
                targetData.put(targetKey, nestedTarget);
            }
        }
    }
    
    /**
     * Process a single mapping rule
     */
    private void processMappingRule(Map<String, Object> sourceData, Map<String, Object> targetData, String targetKey, 
                                   MappingConfiguration.MappingRule rule) {
        
        switch (rule.getMapperType()) {
            case MUSTACHE:
                mustacheMapper.processMapping(sourceData, targetData, targetKey, rule);
                break;
            case COPY:
                Object extractedData = mapPathExtractor.extractPath(sourceData, rule.getJsonPath());
                copyMapper.processMapping(extractedData, targetData, targetKey);
                break;
            case TRANSFORM:
                // Reserved for future custom transformations
                throw new UnsupportedOperationException("TRANSFORM mapper not implemented yet");
        }
    }
}