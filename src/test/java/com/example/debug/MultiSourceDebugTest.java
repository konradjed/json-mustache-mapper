package com.example.debug;

import com.example.JsonStructureMapper;
import com.example.wrapper.MultiSourceDataContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

/**
 * Debug test for multi-source functionality
 */
class MultiSourceDebugTest {
    
    @Test
    void debugMultiSourceContext() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        // Simple test data
        String sourceJson = """
            {
                "orders": [
                    {
                        "orderId": "ORD-001",
                        "productName": "Laptop",
                        "quantity": 1,
                        "price": 1299.99,
                        "status": "shipped"
                    }
                ],
                "user": {
                    "name": "John Doe",
                    "email": "john@example.com"
                }
            }
            """;
        
        JsonNode sourceNode = mapper.readTree(sourceJson);
        
        // Test MultiSourceDataContext directly
        MultiSourceDataContext context = new MultiSourceDataContext();
        
        // Add primary data (order)
        JsonNode orderData = sourceNode.get("orders").get(0);
        context.setPrimaryDataSource(orderData);
        
        // Add secondary data (user)
        JsonNode userData = sourceNode.get("user");
        context.addDataSource("source2", userData);
        
        System.out.println("Context keys: " + context.keySet());
        System.out.println("orderId: " + context.get("orderId"));
        System.out.println("productName: " + context.get("productName"));
        System.out.println("source2: " + context.get("source2"));
        
        if (context.get("source2") != null) {
            System.out.println("source2 type: " + context.get("source2").getClass());
        }
    }
    
    @Test
    void debugFullMultiSourceMapping() throws Exception {
        String sourceJson = """
            {
                "orders": [
                    {
                        "orderId": "ORD-001",
                        "productName": "Laptop",
                        "quantity": 1,
                        "price": 1299.99,
                        "status": "shipped"
                    }
                ],
                "user": {
                    "name": "John Doe",
                    "email": "john@example.com"
                }
            }
            """;
        
        Map<String, Object> config = Map.of(
            "Orders", Map.of(
                "mapperType", "MUSTACHE",
                "templateName", "ORDER_DETAIL",
                "jsonPath", Arrays.asList("$.orders[*]", "$.user")
            )
        );
        
        JsonStructureMapper structureMapper = new JsonStructureMapper();
        String result = structureMapper.transformJsonStructure(sourceJson, config);
        
        System.out.println("Full mapping result:");
        System.out.println(result);
    }
}
