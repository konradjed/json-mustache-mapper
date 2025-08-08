package it.jedrzejewski.mustachemapper;

import it.jedrzejewski.mustachemapper.config.MappingConfiguration;
import it.jedrzejewski.mustachemapper.mapper.MappingProcessor;
import it.jedrzejewski.mustachemapper.template.TemplateRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Main facade for JSON structure mapping operations.
 * Orchestrates the mapping process from source JSON to target JSON structure.
 */
@Data
public class JsonStructureMapper {
    
    private final ObjectMapper objectMapper;
    private final TemplateRegistry templateRegistry;
    private final MappingProcessor mappingProcessor;
    
    public JsonStructureMapper() {
        this.objectMapper = new ObjectMapper();
        this.templateRegistry = new TemplateRegistry();
        this.mappingProcessor = new MappingProcessor(templateRegistry);
    }
    
    /**
     * Transform source JSON to target structure using mapping configuration
     * 
     * @param sourceJson Source JSON string
     * @param mappingConfig Configuration defining the target structure
     * @return Transformed JSON string
     */
    public String transformJsonStructure(String sourceJson, Map<String, Object> mappingConfig) throws IOException {
        // Convert JSON string to Map
        Map<String, Object> sourceData = objectMapper.readValue(sourceJson, new TypeReference<>() {});
        Map<String, Object> targetData = new HashMap<>();
        
        MappingConfiguration config = new MappingConfiguration(mappingConfig);
        mappingProcessor.processMapping(sourceData, targetData, config);
        
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(targetData);
    }
    
    /**
     * Add a custom template to the registry
     */
    public void registerTemplate(String templateName, String templateContent) {
        templateRegistry.addTemplate(templateName, templateContent);
    }
}