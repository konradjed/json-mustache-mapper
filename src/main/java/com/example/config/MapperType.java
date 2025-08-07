package com.example.config;

/**
 * Defines the types of mapping operations available
 */
public enum MapperType {
    /**
     * Process data through Mustache template
     */
    MUSTACHE,
    
    /**
     * Direct copy of JSON fragment
     */
    COPY,
    
    /**
     * Custom transformation (extensible)
     */
    TRANSFORM
}
