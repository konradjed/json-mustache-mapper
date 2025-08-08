package it.jedrzejewski.mustachemapper.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MapPathExtractorTest {
    
    private MapPathExtractor extractor;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Map<String, Object> testData;
    
    @BeforeEach
    void setUp() throws Exception {
        extractor = new MapPathExtractor();

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
        
        testData = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>() {});
    }
    
    @Test
    void testSimplePath() {
        Object result = extractor.extractPath(testData, "$.user");
        assertNotNull(result);
        assertInstanceOf(Map.class, result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> userMap = (Map<String, Object>) result;
        assertEquals("John", userMap.get("name"));
    }
    
    @Test
    void testNestedPath() {
        Object result = extractor.extractPath(testData, "$.user.profile");
        assertNotNull(result);
        assertInstanceOf(Map.class, result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> profileMap = (Map<String, Object>) result;
        assertEquals(30, profileMap.get("age"));
        assertEquals("NYC", profileMap.get("location"));
    }
    
    @Test
    void testArrayPath() {
        Object result = extractor.extractPath(testData, "$.orders");
        assertNotNull(result);
        assertInstanceOf(List.class, result);
        
        @SuppressWarnings("unchecked")
        List<Object> ordersList = (List<Object>) result;
        assertEquals(2, ordersList.size());
    }
    
    @Test
    void testArrayIndexPath() {
        Object result = extractor.extractPath(testData, "$.orders[0]");
        assertNotNull(result);
        assertInstanceOf(Map.class, result);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> orderMap = (Map<String, Object>) result;
        assertEquals("ORD-001", orderMap.get("id"));
        assertEquals("Laptop", orderMap.get("product"));
    }
    
    @Test
    void testArrayWildcardPath() {
        Object result = extractor.extractPath(testData, "$.orders[*]");
        assertNotNull(result);
        assertInstanceOf(List.class, result);
        
        @SuppressWarnings("unchecked")
        List<Object> ordersList = (List<Object>) result;
        assertEquals(2, ordersList.size());
    }
    
    @Test
    void testDeepNestedPath() {
        Object result = extractor.extractPath(testData, "$.user.profile.age");
        assertNotNull(result);
        assertEquals(30, result);
    }
    
    @Test
    void testRootPath() {
        Object result = extractor.extractPath(testData, "$");
        assertNotNull(result);
        assertEquals(testData, result);
    }
    
    @Test
    void testEmptyPath() {
        Object result = extractor.extractPath(testData, "");
        assertNotNull(result);
        assertEquals(testData, result);
    }

    @ParameterizedTest
    @ValueSource(strings = {"$.nonexistent", "$.orders[10]", "$.orders[-1]"})
    void testNonCompliantJsonPaths(String path) {
        Object result = extractor.extractPath(testData, path);
        assertNull(result);
    }
    
    @Test
    void testNullInputs() {
        assertNull(extractor.extractPath(null, "$.test"));
        assertNull(extractor.extractPath(testData, null));
    }
    
    @Test
    void testPathNormalization() {
        // Test different path prefixes
        Object result1 = extractor.extractPath(testData, "$.user.name");
        Object result2 = extractor.extractPath(testData, "$user.name");
        Object result3 = extractor.extractPath(testData, "user.name");
        
        assertEquals("John", result1);
        assertEquals("John", result2);
        assertEquals("John", result3);
    }
    
    @Test
    void testComplexNestedArrayAccess() {
        // Create test data with nested arrays
        Map<String, Object> complexData = Map.of(
            "departments", List.of(
                Map.of("name", "Engineering", 
                       "employees", List.of(
                           Map.of("name", "Alice", "role", "Developer"),
                           Map.of("name", "Bob", "role", "Manager")
                       ))
            )
        );
        
        // Test accessing nested array elements
        Object result = extractor.extractPath(complexData, "$.departments[0].employees[1].name");
        assertEquals("Bob", result);
    }
}