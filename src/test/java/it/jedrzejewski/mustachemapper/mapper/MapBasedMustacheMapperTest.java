package it.jedrzejewski.mustachemapper.mapper;

import it.jedrzejewski.mustachemapper.config.MappingConfiguration;
import it.jedrzejewski.mustachemapper.config.MapperType;
import it.jedrzejewski.mustachemapper.template.TemplateRegistry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Map-based Mustache template functionality
 * Demonstrates the new Map<String, Object> approach instead of JsonNode
 */
class MapBasedMustacheMapperTest {
    
    private MustacheMapper mustacheMapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        TemplateRegistry templateRegistry = new TemplateRegistry();
        mustacheMapper = new MustacheMapper(templateRegistry);
        
        // Add test templates
        templateRegistry.addTemplate("MULTI_SOURCE_TEST", 
            "User: {{name}}, Orders: {{source2.totalOrders}}, Lang: {{source3.language}}");
        templateRegistry.addTemplate("ARRAY_MULTI_SOURCE", 
            "Order: {{orderId}} for {{source2.name}}");
        templateRegistry.addTemplate("USER_SUMMARY", "User: {{name}} ({{email}})");
    }
    
    @Test
    void testMultiSourceSingleMappingWithMap() throws IOException {
        String sourceJson = """
            {
                "user": {"name": "John", "email": "john@example.com"},
                "stats": {"totalOrders": 5, "totalSpent": 100.50},
                "preferences": {"language": "en", "currency": "USD"}
            }
            """;
        
        // Convert JSON to Map
        Map<String, Object> sourceData = objectMapper.readValue(sourceJson, new TypeReference<>() {});
        Map<String, Object> targetData = new HashMap<>();
        
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "MULTI_SOURCE_TEST",
            Arrays.asList("$.user", "$.stats", "$.preferences")
        );
        
        mustacheMapper.processMapping(sourceData, targetData, "result", rule);
        
        String result = (String) targetData.get("result");
        assertEquals("User: John, Orders: 5, Lang: en", result);
    }
    
    @Test
    void testMultiSourceArrayMappingWithMap() throws IOException {
        String sourceJson = """
            {
                "orders": [
                    {"orderId": "ORD001", "amount": 50.0},
                    {"orderId": "ORD002", "amount": 75.0}
                ],
                "user": {"name": "Jane", "email": "jane@example.com"}
            }
            """;
        
        Map<String, Object> sourceData = objectMapper.readValue(sourceJson, new TypeReference<>() {});
        Map<String, Object> targetData = new HashMap<>();
        
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "ARRAY_MULTI_SOURCE",
            Arrays.asList("$.orders[*]", "$.user")
        );
        
        mustacheMapper.processMapping(sourceData, targetData, "results", rule);
        
        @SuppressWarnings("unchecked")
        List<String> resultArray = (List<String>) targetData.get("results");
        assertNotNull(resultArray);
        assertEquals(2, resultArray.size());
        assertEquals("Order: ORD001 for Jane", resultArray.get(0));
        assertEquals("Order: ORD002 for Jane", resultArray.get(1));
    }
    
    @Test
    void testSingleSourceMappingWithMap() throws IOException {
        String sourceJson = """
            {
                "user": {"name": "Bob", "email": "bob@example.com"}
            }
            """;
        
        Map<String, Object> sourceData = objectMapper.readValue(sourceJson, new TypeReference<>() {});
        Map<String, Object> targetData = new HashMap<>();
        
        // Single source mapping
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "USER_SUMMARY",
            "$.user"
        );
        
        mustacheMapper.processMapping(sourceData, targetData, "user", rule);
        
        assertNotNull(targetData.get("user"));
        String result = (String) targetData.get("user");
        assertEquals("User: Bob (bob@example.com)", result);
    }
    
    @Test
    void testDirectMapUsage() {
        // Create data directly as Map (no JSON conversion needed)
        Map<String, Object> userData = Map.of(
            "name", "Alice",
            "email", "alice@example.com"
        );
        
        Map<String, Object> sourceData = Map.of("user", userData);
        Map<String, Object> targetData = new HashMap<>();
        
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "USER_SUMMARY",
            "$.user"
        );
        
        mustacheMapper.processMapping(sourceData, targetData, "summary", rule);
        
        String result = (String) targetData.get("summary");
        assertEquals("User: Alice (alice@example.com)", result);
    }
    
    @Test
    void testMissingDataSourceWithMap() throws IOException {
        String sourceJson = """
            {
                "user": {"name": "Alice"}
            }
            """;
        
        Map<String, Object> sourceData = objectMapper.readValue(sourceJson, new TypeReference<>() {});
        Map<String, Object> targetData = new HashMap<>();
        
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "MULTI_SOURCE_TEST",
            Arrays.asList("$.user", "$.nonexistent", "$.alsoMissing")
        );
        
        // Should not throw exception, missing sources should be handled gracefully
        assertDoesNotThrow(() -> mustacheMapper.processMapping(sourceData, targetData, "result", rule));
        
        // Result should still contain available data
        String result = (String) targetData.get("result");
        assertTrue(result.contains("Alice"));
    }
    
    @Test
    void testNestedPathExtractionWithMap() {
        Map<String, Object> sourceData = Map.of(
            "user", Map.of(
                "profile", Map.of(
                    "name", "Charlie",
                    "preferences", Map.of(
                        "theme", "dark",
                        "language", "es"
                    )
                )
            )
        );
        
        Map<String, Object> targetData = new HashMap<>();
        
        // Test nested path extraction
        MappingConfiguration.MappingRule rule = new MappingConfiguration.MappingRule(
            MapperType.MUSTACHE,
            "USER_SUMMARY",
            "$.user.profile"
        );
        
        mustacheMapper.processMapping(sourceData, targetData, "profile", rule);
        
        String result = (String) targetData.get("profile");
        assertTrue(result.contains("Charlie"));
    }
}