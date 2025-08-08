package it.jedrzejewski.mustachemapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Demonstration of multi-source Mustache template functionality
 */
@Slf4j
public class MultiSourceDemo {
    
    public static void main(String[] args) {
        try {
            MultiSourceDemo demo = new MultiSourceDemo();
            demo.runDemo();
        } catch (Exception e) {
            log.error("Demo failed: {}", e.getMessage(), e);
        }
    }
    
    public void runDemo() throws IOException {
        log.info("=== Multi-Source Mustache Template Demo ===\n");
        
        // Load the multi-source configuration and data
        String configJson = loadResourceFile("src/main/resources/examples/multi-source-config.json");
        String dataJson = loadResourceFile("src/main/resources/examples/multi-source-data.json");
        
        // Parse configuration
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unchecked")
        Map<String, Object> config = mapper.readValue(configJson, Map.class);
        
        // Create mapper and process
        JsonStructureMapper structureMapper = new JsonStructureMapper();
        String result = structureMapper.transformJsonStructure(dataJson, config);
        
        log.info("Input Data:");
        log.info(formatJson(dataJson));
        log.info("\nConfiguration:");
        log.info(formatJson(configJson));
        log.info("\nTransformed Result:");
        log.info(formatJson(result));
        
        // Demonstrate individual template explanations
        demonstrateTemplateExplanations();
    }
    
    private void demonstrateTemplateExplanations() {
        log.info("\n=== Template Explanations ===");
        
        log.info("\n1. ORDER_DETAIL template:");
        log.info("   - Uses data from $.orders[*] (primary array)");
        log.info("   - Additional user data from $.user (accessed via source2)");
        log.info("   - Template: Order ID: {{orderId}}, Customer: {{source2.name}}");
        
        log.info("\n2. USER_WITH_STATS template:");
        log.info("   - Combines $.user, $.statistics, and $.settings.preferences");
        log.info("   - Primary: user data, source2: stats, source3: preferences");
        log.info("   - Template: User: {{name}}, Total Orders: {{source2.totalOrders}}");
        
        log.info("\n3. PRODUCT_RECOMMENDATIONS template:");
        log.info("   - Primary: $.recommendations[*] (array processing)");
        log.info("   - source2: $.user.preferences, source3: $.marketingData");
        log.info("   - Template: {{name}} for {{source2.category}} lover");
        
        log.info("\n4. SingleSourceOrder (backward compatibility):");
        log.info("   - Uses single JSONPath: $.orders[0]");
        log.info("   - Works exactly like the original implementation");
    }
    
    private String loadResourceFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }
    
    private String formatJson(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object obj = mapper.readValue(json, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }
}
