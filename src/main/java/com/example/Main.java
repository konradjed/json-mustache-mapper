package com.example;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Main application demonstrating the JSON structure mapping capabilities
 */
@Slf4j
public class Main {

    public static void main(String[] args) {
        JsonStructureMapper mapper = new JsonStructureMapper();
        
        try {
            // Sample source JSON
            String sourceJson = """
                {
                    "user": {
                        "name": "John Doe",
                        "email": "john.doe@example.com",
                        "profile": {
                            "age": 30,
                            "location": "New York"
                        }
                    },
                    "orders": [
                        {
                            "orderId": "ORD-001",
                            "productName": "Laptop",
                            "price": 1299.99,
                            "quantity": 1,
                            "status": "shipped"
                        },
                        {
                            "orderId": "ORD-002",
                            "productName": "Mouse",
                            "price": 29.99,
                            "quantity": 2,
                            "status": "pending"
                        }
                    ],
                    "settings": {
                        "theme": "dark",
                        "notifications": true
                    }
                }
                """;
            
            // Define mapping configuration
            Map<String, Object> mappingConfig = new HashMap<>();
            Map<String, Object> clientConfig = new HashMap<>();
            
            // Orders mapping - process each order with template
            Map<String, Object> ordersMapping = new HashMap<>();
            ordersMapping.put("mapperType", "MUSTACHE");
            ordersMapping.put("templateName", "ORDER_DETAIL");
            ordersMapping.put("jsonPath", "$.orders[*]");
            clientConfig.put("Orders", ordersMapping);
            
            // User info mapping - process user with template
            Map<String, Object> userMapping = new HashMap<>();
            userMapping.put("mapperType", "MUSTACHE");
            userMapping.put("templateName", "USER_SUMMARY");
            userMapping.put("jsonPath", "$.user");
            clientConfig.put("UserInfo", userMapping);
            
            // Settings mapping - direct copy
            Map<String, Object> settingsMapping = new HashMap<>();
            settingsMapping.put("mapperType", "COPY");
            settingsMapping.put("jsonPath", "$.settings");
            clientConfig.put("Settings", settingsMapping);
            
            mappingConfig.put("client", clientConfig);
            
            // Transform JSON structure
            String result = mapper.transformJsonStructure(sourceJson, mappingConfig);
            
            log.info("🎯 JSON Structure Mapping Result:");
            log.info("=".repeat(50));
            log.info(result);
            
            // Demonstrate template registration
            mapper.registerTemplate("CUSTOM_ORDER", "📦 {{product}} - {{quantity}} units @ ${{price}}");
            log.info("\n✅ Custom template registered successfully!");
            
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage());
            e.printStackTrace();
        }
    }
}
