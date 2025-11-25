package datahub.core;

import java.util.Properties;

/**
 * Singleton class for centralizing core configuration parameters.
 */
public class CoreParameters {

    private static CoreParameters instance;
    private final Properties properties;

    private CoreParameters() {
        properties = new Properties();
        // Load defaults or read from config file here
    }

    public static synchronized CoreParameters getInstance() {
        if (instance == null) {
            instance = new CoreParameters();
        }
        return instance;
    }

    public String getParameter(String key) {
        return properties.getProperty(key);
    }

    public void setParameter(String key, String value) {
        properties.setProperty(key, value);
    }
}
