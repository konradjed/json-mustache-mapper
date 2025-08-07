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
            Order ID: {{orderId}}
            Product: {{productName}} ({{quantity}}x)
            Price: ${{price}}
            Status: {{status}}
            Customer: {{source2.name}} ({{source2.email}})
            """);
            
        addTemplate("USER_SUMMARY", """
            {{name}} ({{email}})
            Age: {{profile.age}}, Location: {{profile.location}}
            """);
            
        addTemplate("SETTINGS_INFO", """
            Theme: {{theme}}, Notifications: {{notifications}}
            """);
            
        // Multi-source templates
        addTemplate("USER_WITH_STATS", """
            User: {{name}} ({{email}})
            Membership: {{membershipLevel}}
            Total Orders: {{source2.totalOrders}}
            Total Spent: ${{source2.totalSpent}}
            Language: {{source3.language}}
            Currency: {{source3.currency}}
            """);
            
        addTemplate("PRODUCT_RECOMMENDATIONS", """
            Recommended: {{name}} (Score: {{score}})
            For {{source2.category}} lover {{source2.priceRange}}
            Campaign: {{source3.campaignId}} - {{source3.discount}}% off
            """);
            
        addTemplate("SIMPLE_ORDER", """
            Order: {{orderId}} - {{productName}}
            Qty: {{quantity}}, Price: ${{price}}
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
