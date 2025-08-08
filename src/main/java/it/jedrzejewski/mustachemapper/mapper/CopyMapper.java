package it.jedrzejewski.mustachemapper.mapper;

import java.util.Map;

/**
 * Handles direct copying of data fragments without transformation
 */
public class CopyMapper {
    
    /**
     * Copy data fragment directly to target
     */
    public void processMapping(Object extractedData, Map<String, Object> targetData, String targetKey) {
        if (extractedData != null) {
            targetData.put(targetKey, extractedData);
        }
    }
}