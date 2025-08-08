package it.jedrzejewski.mustachemapper.debug;

import it.jedrzejewski.mustachemapper.JsonStructureMapper;
import it.jedrzejewski.mustachemapper.wrapper.MultiSourceDataContext;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Debug test for multi-source functionality using Map-based approach
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
        
        Map<String, Object> sourceData = mapper.readValue(sourceJson, new TypeReference<>() {});
        
        // Test MultiSourceDataContext directly
        MultiSourceDataContext context = new MultiSourceDataContext();
        
        // Add primary data (order)
        @SuppressWarnings("unchecked")
        Map<String, Object> ordersArray = (Map<String, Object>) ((java.util.List<?>) sourceData.get("orders")).get(0);
        context.setPrimaryDataSource(ordersArray);
        
        // Add secondary data (user)
        @SuppressWarnings("unchecked")
        Map<String, Object> userData = (Map<String, Object>) sourceData.get("user");
        context.addDataSource("source2", userData);
        
        System.out.println("Context keys: " + context.keySet());
        System.out.println("orderId: " + context.get("orderId"));
        System.out.println("productName: " + context.get("productName"));
        System.out.println("source2: " + context.get("source2"));
        
        if (context.get("source2") != null) {
            System.out.println("source2 type: " + context.get("source2").getClass());
        }

        assertTrue(context.get("orderId").toString().contains("ORD-001"));
        assertTrue(context.get("productName").toString().contains("Laptop"));
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
        assertTrue(result.contains("ORD-001"));
        assertTrue(result.contains("John Doe (john@example.com)"));
    }
}