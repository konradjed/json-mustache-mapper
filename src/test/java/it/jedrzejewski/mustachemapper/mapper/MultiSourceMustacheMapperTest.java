package it.jedrzejewski.mustachemapper.mapper;

import it.jedrzejewski.mustachemapper.config.MappingConfiguration;
import it.jedrzejewski.mustachemapper.config.MapperType;
import it.jedrzejewski.mustachemapper.template.TemplateRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for multi-source Mustache template functionality
 */
class MultiSourceMustacheMapperTest {
    
    private MustacheMapper mustacheMapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        TemplateRegistry templateRegistry = new TemplateRegistry();
        mustacheMapper = new MustacheMapper(objectMapper, templateRegistry);
        
        // Add test templates
        templateRegistry.addTemplate("MULTI_SOURCE_TEST", 
            "User: {{name}}, Orders: {{source2.totalOrders}}, Lang: {{source3.language}}");
        templateRegistry.addTemplate("ARRAY_MULTI_SOURCE", 
            "Order: {{orderId}} for {{source2.name}}");
    }
    
    @Test
    void testMultiSourceSingleMapping() throws IOException {
        String sourceJson = """
            {
                "user": {"name": "John", "email": "john@example.com"},
                "stats": {"totalOrders": 5, "totalSpent": 100.50},
                "preferences": {"language": "en", "currency": "USD"}
            }
            """;
        
        JsonNode sourceNode = objectMapper.readTree(sourceJson);
        ObjectNode targetNode = objectMapper.createObjectNode();
        
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "MULTI_SOURCE_TEST",
            Arrays.asList("$.user", "$.stats", "$.preferences")
        );
        
        mustacheMapper.processMapping(sourceNode, targetNode, "result", rule);
        
        String result = targetNode.get("result").asText();
        assertEquals("User: John, Orders: 5, Lang: en", result);
    }
    
    @Test
    void testMultiSourceArrayMapping() throws IOException {
        String sourceJson = """
            {
                "orders": [
                    {"orderId": "ORD001", "amount": 50.0},
                    {"orderId": "ORD002", "amount": 75.0}
                ],
                "user": {"name": "Jane", "email": "jane@example.com"}
            }
            """;
        
        JsonNode sourceNode = objectMapper.readTree(sourceJson);
        ObjectNode targetNode = objectMapper.createObjectNode();
        
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "ARRAY_MULTI_SOURCE",
            Arrays.asList("$.orders[*]", "$.user")
        );
        
        mustacheMapper.processMapping(sourceNode, targetNode, "results", rule);
        
        JsonNode resultArray = targetNode.get("results");
        assertTrue(resultArray.isArray());
        assertEquals(2, resultArray.size());
        assertEquals("Order: ORD001 for Jane", resultArray.get(0).asText());
        assertEquals("Order: ORD002 for Jane", resultArray.get(1).asText());
    }
    
    @Test
    void testBackwardCompatibilitySingleSource() throws IOException {
        String sourceJson = """
            {
                "user": {"name": "Bob", "email": "bob@example.com"}
            }
            """;
        
        JsonNode sourceNode = objectMapper.readTree(sourceJson);
        ObjectNode targetNode = objectMapper.createObjectNode();
        
        // Single source using old constructor
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "USER_SUMMARY",
            "$.user"
        );
        
        mustacheMapper.processMapping(sourceNode, targetNode, "user", rule);
        
        assertNotNull(targetNode.get("user"));
        String result = targetNode.get("user").asText();
        assertTrue(result.contains("Bob"));
    }
    
    @Test
    void testMissingDataSource() throws IOException {
        String sourceJson = """
            {
                "user": {"name": "Alice"}
            }
            """;
        
        JsonNode sourceNode = objectMapper.readTree(sourceJson);
        ObjectNode targetNode = objectMapper.createObjectNode();
        
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "MULTI_SOURCE_TEST",
            Arrays.asList("$.user", "$.nonexistent", "$.alsoMissing")
        );
        
        // Should not throw exception, missing sources should be handled gracefully
        assertDoesNotThrow(() -> mustacheMapper.processMapping(sourceNode, targetNode, "result", rule));
        
        // Result should still contain available data
        String result = targetNode.get("result").asText();
        assertTrue(result.contains("Alice"));
    }
    
    @Test
    void testEmptyJsonPathList() throws IOException {
        String sourceJson = """
            {
                "user": {"name": "Charlie"}
            }
            """;
        
        JsonNode sourceNode = objectMapper.readTree(sourceJson);
        ObjectNode targetNode = objectMapper.createObjectNode();
        
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "USER_SUMMARY",
                List.of()
        );
        
        // Should handle empty path list gracefully
        assertDoesNotThrow(() -> mustacheMapper.processMapping(sourceNode, targetNode, "result", rule));
    }
}
