package it.jedrzejewski.mustachemapper.integration;

import it.jedrzejewski.mustachemapper.JsonStructureMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for multi-source Mustache functionality
 */
class MultiSourceIntegrationTest {
    
    @Test
    void testEndToEndMultiSourceMapping() throws Exception {
        String sourceJson = """
            {
                "user": {
                    "name": "John Doe",
                    "email": "john@example.com",
                    "membershipLevel": "gold"
                },
                "orders": [
                    {
                        "orderId": "ORD001",
                        "productName": "Wireless Headphones",
                        "quantity": 1,
                        "price": 99.99,
                        "status": "shipped"
                    }
                ],
                "statistics": {
                    "totalOrders": 15,
                    "totalSpent": 1249.87
                },
                "settings": {
                    "preferences": {
                        "language": "en",
                        "currency": "USD"
                    }
                }
            }
            """;
        
        Map<String, Object> config = Map.of(
            "client", Map.of(
                "orderWithUser", Map.of(
                    "mapperType", "MUSTACHE",
                    "templateName", "ORDER_DETAIL", 
                    "jsonPath", Arrays.asList("$.orders[*]", "$.user")
                ),
                "userProfile", Map.of(
                    "mapperType", "MUSTACHE",
                    "templateName", "USER_WITH_STATS",
                    "jsonPath", Arrays.asList("$.user", "$.statistics", "$.settings.preferences")
                )
            )
        );
        
        JsonStructureMapper mapper = new JsonStructureMapper();
        String result = mapper.transformJsonStructure(sourceJson, config);
        
        // Verify the result structure
        ObjectMapper objectMapper = new ObjectMapper();
        Map<?, ?> resultMap = objectMapper.readValue(result, Map.class);
        
        assertTrue(resultMap.containsKey("client"));
        Map<?, ?> clientMap = (Map<?, ?>) resultMap.get("client");
        
        assertTrue(clientMap.containsKey("orderWithUser"));
        assertTrue(clientMap.containsKey("userProfile"));
        
        // Verify order contains user information
        Object orderResult = clientMap.get("orderWithUser");
        assertNotNull(orderResult);
        String orderText = orderResult.toString();
        assertTrue(orderText.contains("ORD001"));
        assertTrue(orderText.contains("John Doe"));
        
        // Verify user profile contains statistics
        String userProfileText = clientMap.get("userProfile").toString();
        assertTrue(userProfileText.contains("John Doe"));
        assertTrue(userProfileText.contains("15")); // totalOrders
        assertTrue(userProfileText.contains("en")); // language
        
        System.out.println("Integration test result:");
        System.out.println(result);
    }
    
    @Test
    void testBackwardCompatibility() throws Exception {
        String sourceJson = """
            {
                "user": {
                    "name": "Jane Smith",
                    "email": "jane@example.com"
                }
            }
            """;
        
        Map<String, Object> config = Map.of(
            "result", Map.of(
                "mapperType", "MUSTACHE",
                "templateName", "USER_SUMMARY",
                "jsonPath", "$.user"
            )
        );
        
        JsonStructureMapper mapper = new JsonStructureMapper();
        String result = mapper.transformJsonStructure(sourceJson, config);
        
        // Should work exactly like before
        assertNotNull(result);
        assertTrue(result.contains("Jane Smith"));
        
        System.out.println("Backward compatibility test result:");
        System.out.println(result);
    }
}
