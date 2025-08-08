package it.jedrzejewski.mustachemapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Main application demonstrating the JSON structure mapping capabilities
 */
public class Main {
    
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static final String MAPPER_TYPE = "mapperType";
    public static final String TEMPLATE_NAME = "templateName";
    public static final String JSON_PATH = "jsonPath";

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
            ordersMapping.put(MAPPER_TYPE, "MUSTACHE");
            ordersMapping.put(TEMPLATE_NAME, "ORDER_DETAIL");
            ordersMapping.put(JSON_PATH, "$.orders[*]");
            clientConfig.put("Orders", ordersMapping);
            
            // User info mapping - process user with template
            Map<String, Object> userMapping = new HashMap<>();
            userMapping.put(MAPPER_TYPE, "MUSTACHE");
            userMapping.put(TEMPLATE_NAME, "USER_SUMMARY");
            userMapping.put(JSON_PATH, "$.user");
            clientConfig.put("UserInfo", userMapping);
            
            // Settings mapping - direct copy
            Map<String, Object> settingsMapping = new HashMap<>();
            settingsMapping.put(MAPPER_TYPE, "COPY");
            settingsMapping.put(JSON_PATH, "$.settings");
            clientConfig.put("Settings", settingsMapping);
            
            mappingConfig.put("client", clientConfig);
            
            // Transform JSON structure
            String result = mapper.transformJsonStructure(sourceJson, mappingConfig);
            
            if (log.isInfoEnabled()) {
                log.info("🎯 JSON Structure Mapping Result:");
                log.info("=".repeat(50));
                log.info(result);
            }

            // Demonstrate template registration
            mapper.registerTemplate("CUSTOM_ORDER", "📦 {{product}} - {{quantity}} units @ ${{price}}");
            log.info("\n✅ Custom template registered successfully!");
            
        } catch (Exception e) {
            log.error("❌ Error: {}", e.getMessage(), e);
        }
    }
}
