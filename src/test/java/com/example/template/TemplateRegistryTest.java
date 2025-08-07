package com.example.template;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TemplateRegistryTest {
    
    private TemplateRegistry registry;
    
    @BeforeEach
    void setUp() {
        registry = new TemplateRegistry();
    }
    
    @Test
    void testDefaultTemplatesExist() {
        assertTrue(registry.hasTemplate("ORDER_DETAIL"));
        assertTrue(registry.hasTemplate("USER_SUMMARY"));
        assertTrue(registry.hasTemplate("SETTINGS_INFO"));
        assertEquals(3, registry.getTemplateCount());
    }
    
    @Test
    void testAddTemplate() {
        registry.addTemplate("TEST_TEMPLATE", "Hello {{name}}!");
        
        assertTrue(registry.hasTemplate("TEST_TEMPLATE"));
        assertEquals("Hello {{name}}!", registry.getTemplate("TEST_TEMPLATE"));
        assertEquals(4, registry.getTemplateCount());
    }
    
    @Test
    void testAddTemplateValidation() {
        assertThrows(IllegalArgumentException.class, () -> 
            registry.addTemplate(null, "content"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            registry.addTemplate("", "content"));
        
        assertThrows(IllegalArgumentException.class, () -> 
            registry.addTemplate("name", null));
    }
    
    @Test
    void testGetNonExistentTemplate() {
        assertThrows(IllegalArgumentException.class, () -> 
            registry.getTemplate("NON_EXISTENT"));
    }
    
    @Test
    void testRemoveTemplate() {
        registry.addTemplate("TEMP", "content");
        assertTrue(registry.hasTemplate("TEMP"));
        
        boolean removed = registry.removeTemplate("TEMP");
        assertTrue(removed);
        assertFalse(registry.hasTemplate("TEMP"));
        
        boolean removedAgain = registry.removeTemplate("TEMP");
        assertFalse(removedAgain);
    }
    
    @Test
    void testGetTemplateNames() {
        var names = registry.getTemplateNames();
        assertTrue(names.contains("ORDER_DETAIL"));
        assertTrue(names.contains("USER_SUMMARY"));
        assertTrue(names.contains("SETTINGS_INFO"));
    }
}
