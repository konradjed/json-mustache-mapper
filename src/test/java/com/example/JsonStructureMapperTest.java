package com.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;

class JsonStructureMapperTest {
    
    private JsonStructureMapper mapper;
    
    @BeforeEach
    void setUp() {
        mapper = new JsonStructureMapper();
    }
    
    @Test
    void testOrdersMapping() throws Exception {
        String sourceJson = """
            {
                "orders": [
                    {"orderId": "ORD-001", "productName": "Laptop", "price": 1299.99, "quantity": 1, "status": "shipped"}
                ]
            }
            """;
        
        Map<String, Object> config = Map.of(
            "client", Map.of(
                "Orders", Map.of(
                    "mapperType", "MUSTACHE",
                    "templateName", "ORDER_DETAIL",
                    "jsonPath", "$.orders[*]"
                )
            )
        );
        
        String result = mapper.transformJsonStructure(sourceJson, config);
        
        assertNotNull(result);
        System.out.println("Orders test result: " + result);
        assertTrue(result.contains("ORD-001"), "Result should contain ORD-001. Actual result: " + result);
        assertTrue(result.contains("Laptop"), "Result should contain Laptop. Actual result: " + result);
    }
    
    @Test
    void testCopyMapping() throws Exception {
        String sourceJson = """
            {
                "settings": {"theme": "dark", "notifications": true}
            }
            """;
        
        Map<String, Object> config = Map.of(
            "client", Map.of(
                "Settings", Map.of(
                    "mapperType", "COPY",
                    "jsonPath", "$.settings"
                )
            )
        );
        
        String result = mapper.transformJsonStructure(sourceJson, config);
        
        assertNotNull(result);
        assertTrue(result.contains("dark"));
        assertTrue(result.contains("notifications"));
    }
    
    @Test
    void testTemplateRegistration() {
        mapper.registerTemplate("TEST_TEMPLATE", "Test: {{name}}");
        assertTrue(mapper.getTemplateRegistry().hasTemplate("TEST_TEMPLATE"));
        assertEquals("Test: {{name}}", mapper.getTemplateRegistry().getTemplate("TEST_TEMPLATE"));
    }
    
    @Test
    void testComplexMapping() throws Exception {
        String sourceJson = """
            {
                "user": {"name": "John", "email": "john@example.com", "profile": {"age": 30, "location": "NYC"}},
                "orders": [{"orderId": "ORD-001", "productName": "Book", "price": 15.99, "quantity": 2, "status": "pending"}],
                "settings": {"theme": "light", "notifications": false}
            }
            """;
        
        Map<String, Object> config = new HashMap<>();
        Map<String, Object> clientConfig = new HashMap<>();
        
        clientConfig.put("UserInfo", Map.of(
            "mapperType", "MUSTACHE",
            "templateName", "USER_SUMMARY",
            "jsonPath", "$.user"
        ));
        
        clientConfig.put("Orders", Map.of(
            "mapperType", "MUSTACHE",
            "templateName", "ORDER_DETAIL",
            "jsonPath", "$.orders[*]"
        ));
        
        clientConfig.put("Settings", Map.of(
            "mapperType", "COPY",
            "jsonPath", "$.settings"
        ));
        
        config.put("client", clientConfig);
        
        String result = mapper.transformJsonStructure(sourceJson, config);
        
        assertNotNull(result);
        System.out.println("Complex test result: " + result);
        assertTrue(result.contains("John"), "Result should contain John. Actual result: " + result);
        assertTrue(result.contains("ORD-001"), "Result should contain ORD-001. Actual result: " + result);
        assertTrue(result.contains("light"), "Result should contain light. Actual result: " + result);
    }
}
