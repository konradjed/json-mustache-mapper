package it.jedrzejewski.mustachemapper.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonPathExtractorTest {
    
    private JsonPathExtractor extractor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private JsonNode testJson;
    
    @BeforeEach
    void setUp() throws Exception {
        extractor = new JsonPathExtractor();

        String jsonString = """
            {
                "user": {
                    "name": "John",
                    "profile": {"age": 30, "location": "NYC"}
                },
                "orders": [
                    {"id": "ORD-001", "product": "Laptop"},
                    {"id": "ORD-002", "product": "Mouse"}
                ],
                "settings": {"theme": "dark"}
            }
            """;
        
        testJson = objectMapper.readTree(jsonString);
    }
    
    @Test
    void testSimplePath() {
        JsonNode result = extractor.extractPath(testJson, "$.user");
        assertNotNull(result);
        assertEquals("John", result.get("name").asText());
    }
    
    @Test
    void testNestedPath() {
        JsonNode result = extractor.extractPath(testJson, "$.user.profile");
        assertNotNull(result);
        assertEquals(30, result.get("age").asInt());
        assertEquals("NYC", result.get("location").asText());
    }
    
    @Test
    void testArrayPath() {
        JsonNode result = extractor.extractPath(testJson, "$.orders");
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(2, result.size());
    }
    
    @Test
    void testArrayIndexPath() {
        JsonNode result = extractor.extractPath(testJson, "$.orders[0]");
        assertNotNull(result);
        assertEquals("ORD-001", result.get("id").asText());
        assertEquals("Laptop", result.get("product").asText());
    }
    
    @Test
    void testArrayWildcardPath() {
        JsonNode result = extractor.extractPath(testJson, "$.orders[*]");
        assertNotNull(result);
        assertTrue(result.isArray());
        assertEquals(2, result.size());
    }
    
    @Test
    void testDeepNestedPath() {
        JsonNode result = extractor.extractPath(testJson, "$.user.profile.age");
        assertNotNull(result);
        assertEquals(30, result.asInt());
    }
    
    @Test
    void testRootPath() {
        JsonNode result = extractor.extractPath(testJson, "$");
        assertNotNull(result);
        assertEquals(testJson, result);
    }
    
    @Test
    void testNonExistentPath() {
        JsonNode result = extractor.extractPath(testJson, "$.nonexistent");
        assertNull(result);
    }
    
    @Test
    void testInvalidArrayIndex() {
        JsonNode result = extractor.extractPath(testJson, "$.orders[10]");
        assertNull(result);
    }
    
    @Test
    void testNullInputs() {
        assertNull(extractor.extractPath(null, "$.test"));
        assertNull(extractor.extractPath(testJson, null));
    }
}
