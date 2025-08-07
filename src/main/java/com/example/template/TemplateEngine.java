package com.example.template;

import com.example.wrapper.JsonNodeWrapper;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.StringReader;
import java.io.StringWriter;

/**
 * Handles Mustache template compilation and rendering
 */
public class TemplateEngine {
    
    private final TemplateRegistry templateRegistry;
    private final MustacheFactory mustacheFactory;
    
    public TemplateEngine(TemplateRegistry templateRegistry) {
        this.templateRegistry = templateRegistry;
        this.mustacheFactory = new DefaultMustacheFactory();
    }
    
    /**
     * Render template with data wrapper
     */
    public String render(String templateName, JsonNodeWrapper dataWrapper) {
        String templateContent = templateRegistry.getTemplate(templateName);
        return renderTemplate(templateContent, dataWrapper);
    }
    
    /**
     * Render template with any data object (supports both JsonNodeWrapper and MultiSourceDataContext)
     */
    public String render(String templateName, Object dataObject) {
        String templateContent = templateRegistry.getTemplate(templateName);
        return renderTemplate(templateContent, dataObject);
    }
    
    /**
     * Render template string with data wrapper
     */
    public String renderTemplate(String templateContent, JsonNodeWrapper dataWrapper) {
        return renderTemplate(templateContent, (Object) dataWrapper);
    }
    
    /**
     * Render template string with any data object
     */
    public String renderTemplate(String templateContent, Object dataObject) {
        Mustache mustache = mustacheFactory.compile(new StringReader(templateContent), "template");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, dataObject);
        return writer.toString().trim();
    }
}
