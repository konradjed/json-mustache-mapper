package com.example.wrapper;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;

/**
 * Data context that holds multiple JSON data sources for Mustache templates.
 * Allows templates to access data from multiple JSONPath expressions.
 * Extends HashMap to be directly usable by Mustache templates.
 */
public class MultiSourceDataContext extends HashMap<String, Object> {
    
    public MultiSourceDataContext() {
        super();
    }
    
    /**
     * Add a data source with a specific key
     */
    public void addDataSource(String key, JsonNode data) {
        if (data != null) {
            this.put(key, new JsonNodeWrapper(data));
        }
    }
    
    /**
     * Add the primary data source (accessible without a key prefix)
     */
    public void setPrimaryDataSource(JsonNode data) {
        if (data != null) {
            JsonNodeWrapper wrapper = new JsonNodeWrapper(data);
            // Add all properties from the primary source directly to the context
            if (data.isObject()) {
                data.fields().forEachRemaining(entry ->
                        this.put(entry.getKey(), wrapper.get(entry.getKey())));
            } else {
                // For non-object types, add the wrapper directly
                this.put("value", wrapper);
            }
        }
    }
    
    /**
     * Add a simple key-value pair
     */
    public void addValue(String key, Object value) {
        this.put(key, value);
    }
}
