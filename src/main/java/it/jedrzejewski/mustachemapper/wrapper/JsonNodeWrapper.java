package it.jedrzejewski.mustachemapper.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Wrapper that provides seamless access to JsonNode properties in Mustache templates
 * This class implements Map-like behavior to work with Mustache.java
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class JsonNodeWrapper extends HashMap<String, Object> {
    
    private final transient JsonNode node;
    
    public JsonNodeWrapper(JsonNode node) {
        super();
        this.node = node;
        initializeDynamicProperties();
    }
    
    /**
     * Initialize all properties dynamically from the JsonNode
     */
    private void initializeDynamicProperties() {
        if (node != null && node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();
                this.put(key, convertJsonValue(value));
            }
        }
        
        // Add special properties
        if (node != null && node.isArray()) {
            this.put("size", node.size());
        }
    }
    
    /**
     * Convert JsonNode to appropriate Java object for Mustache
     */
    private Object convertJsonValue(JsonNode jsonNode) {
        if (jsonNode == null || jsonNode.isNull()) {
            return null;
        } else if (jsonNode.isBoolean()) {
            return jsonNode.booleanValue();
        } else if (jsonNode.isNumber()) {
            if (jsonNode.isInt()) {
                return jsonNode.intValue();
            } else if (jsonNode.isLong()) {
                return jsonNode.longValue();
            } else {
                return jsonNode.doubleValue();
            }
        } else if (jsonNode.isTextual()) {
            return jsonNode.textValue();
        } else if (jsonNode.isArray()) {
            List<JsonNodeWrapper> list = new ArrayList<>();
            for (JsonNode item : jsonNode) {
                list.add(new JsonNodeWrapper(item));
            }
            return list;
        } else if (jsonNode.isObject()) {
            return new JsonNodeWrapper(jsonNode);
        }
        return jsonNode.toString();
    }
    
    /**
     * Override get to provide fallback behavior for property access
     */
    @Override
    public Object get(Object key) {
        // First try the map
        Object value = super.get(key);
        if (value != null) {
            return value;
        }
        
        // Fallback to direct node access
        if (key instanceof String keyName) {

            if ("size".equals(keyName) && node != null && node.isArray()) {
                return node.size();
            }
            
            if (keyName.contains(".")) {
                return getValueByPath(keyName);
            }
            
            if (node != null && node.has(keyName)) {
                return convertJsonValue(node.get(keyName));
            }
        }
        
        return null;
    }
    
    /**
     * Support for dot-notation paths
     */
    private Object getValueByPath(String path) {
        String[] parts = path.split("\\.");
        JsonNode current = node;
        
        for (String part : parts) {
            if (current == null) return null;
            
            if (part.contains("[") && part.contains("]")) {
                String arrayField = part.substring(0, part.indexOf("["));
                int index = Integer.parseInt(part.substring(part.indexOf("[") + 1, part.indexOf("]")));
                
                current = current.get(arrayField);
                if (current != null && current.isArray() && index < current.size()) {
                    current = current.get(index);
                } else {
                    return null;
                }
            } else {
                current = current.get(part);
            }
        }
        
        return convertJsonValue(current);
    }
    
    @Override
    public String toString() {
        return node != null ? node.toString() : "null";
    }
}
