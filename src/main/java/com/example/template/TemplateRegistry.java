package com.example.template;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Registry for managing Mustache templates
 */
public class TemplateRegistry {
    
    private final Map<String, String> templates;
    
    public TemplateRegistry() {
        this.templates = new HashMap<>();
        initializeDefaultTemplates();
    }
    
    /**
     * Initialize with default templates
     */
    private void initializeDefaultTemplates() {
        addTemplate("ORDER_DETAIL", """
            Order ID: {{id}}
            Product: {{product}} ({{quantity}}x)
            Price: ${{price}}
            Status: {{status}}
            """);
            
        addTemplate("USER_SUMMARY", """
            {{name}} ({{email}})
            Age: {{profile.age}}, Location: {{profile.location}}
            """);
            
        addTemplate("SETTINGS_INFO", """
            Theme: {{theme}}, Notifications: {{notifications}}
            """);
    }
    
    /**
     * Add or update a template
     */
    public void addTemplate(String name, String templateContent) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name cannot be null or empty");
        }
        if (templateContent == null) {
            throw new IllegalArgumentException("Template content cannot be null");
        }
        templates.put(name, templateContent);
    }
    
    /**
     * Get template by name
     */
    public String getTemplate(String name) {
        String template = templates.get(name);
        if (template == null) {
            throw new IllegalArgumentException("Template not found: " + name);
        }
        return template;
    }
    
    /**
     * Check if template exists
     */
    public boolean hasTemplate(String name) {
        return templates.containsKey(name);
    }
    
    /**
     * Get all template names
     */
    public Set<String> getTemplateNames() {
        return templates.keySet();
    }
    
    /**
     * Remove template
     */
    public boolean removeTemplate(String name) {
        return templates.remove(name) != null;
    }
    
    /**
     * Get template count
     */
    public int getTemplateCount() {
        return templates.size();
    }
}
