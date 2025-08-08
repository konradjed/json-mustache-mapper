package it.jedrzejewski.mustachemapper.wrapper;

import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Data context that holds multiple data sources for Mustache templates.
 * Allows templates to access data from multiple JSONPath expressions.
 * Extends HashMap to be directly usable by Mustache templates.
 */
@NoArgsConstructor
public class MultiSourceDataContext extends HashMap<String, Object> {
    /**
     * Add a data source with a specific key
     */
    public void addDataSource(String key, Map<String, Object> data) {
        if (data != null) {
            this.put(key, data);
        }
    }
    
    /**
     * Add the primary data source (accessible without a key prefix)
     */
    public void setPrimaryDataSource(Map<String, Object> data) {
        if (data != null) {
            // Add all properties from the primary source directly to the context
            this.putAll(data);
        }
    }
    
    /**
     * Add a simple key-value pair
     */
    public void addValue(String key, Object value) {
        this.put(key, value);
    }
}