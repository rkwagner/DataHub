package datahub.hudi;

import java.util.HashMap;
import java.util.Map;

import datahub.core.contract.Properties;

/**
 * Hudi-specific properties implementation using Hudi's actual configuration
 * keys.
 * This class stores configuration as key-value pairs matching Hudi's expected
 * format.
 */
public class HudiProperties extends Properties<HudiType> {

    public static final String TIMESTAMP_PRECISION_KEY = "datahub.hudi.timestamp.precision";

    private final Map<String, Object> properties = new HashMap<>();

    /**
     * Set a property using a Hudi configuration key.
     * 
     * @param key   The Hudi configuration key (e.g., from HoodieTableConfig)
     * @param value The configuration value
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Get a property value by its Hudi configuration key.
     * 
     * @param key The Hudi configuration key
     * @return The configuration value, or null if not set
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Get a property value as a String.
     * 
     * @param key The Hudi configuration key
     * @return The configuration value as a String, or null if not set
     */
    public String getStringProperty(String key) {
        Object value = properties.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * Get all properties as a Map for passing to Hudi operations.
     * 
     * @return Map of all configuration properties
     */
    public Map<String, Object> getAllProperties() {
        return new HashMap<>(properties);
    }

    /**
     * Get all properties as a Map with String values.
     * 
     * @return Map of all configuration properties with String values
     */
    public Map<String, String> getAllPropertiesAsStrings() {
        Map<String, String> stringMap = new HashMap<>();
        properties.forEach((key, value) -> {
            if (value != null) {
                stringMap.put(key, value.toString());
            }
        });
        return stringMap;
    }
}
