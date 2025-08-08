package it.jedrzejewski.mustachemapper.wrapper;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.NoArgsConstructor;

import java.util.HashMap;

/**
 * Data context that holds multiple JSON data sources for Mustache templates.
 * Allows templates to access data from multiple JSONPath expressions.
 * Extends HashMap to be directly usable by Mustache templates.
 */
@NoArgsConstructor
public class MultiSourceDataContext extends HashMap<String, Object> {
    /**
     * Add a data source with a specific key
     */
    public void addDataSource(String key, JsonNode data) {
        if (data != null) {
            addValue(key, new JsonNodeWrapper(data));
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
                        addValue(entry.getKey(), wrapper.get(entry.getKey())));
            } else {
                // For non-object types, add the wrapper directly
                addValue("value", wrapper);
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
