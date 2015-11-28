package com.cluedrive.commons;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesUtility {
    /**
     * Reads value from application properties.
     * @param key The key of the property which needs to be returned.
     * @return The value for the given property key.
     */
    public static String applicationProperty(String key) {
        Properties properties = new Properties();
        try (InputStream inputStream = PropertiesUtility.class.getResourceAsStream("/config.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("Application properties file missing, or just property " + key + " is missing");
        }
        return properties.getProperty(key);
    }

    /**
     * Reads value from API properties.
     * @param key The key of the property which needs to be returned.
     * @return The value for the given property key.
     */
    public static String apiProperty(String key) {
        Properties properties = new Properties();
        try (InputStream inputStream = PropertiesUtility.class.getResourceAsStream("/apiconfig.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("API properties file missing, or just property " + key + " is missing");
        }
        return properties.getProperty(key);
    }
}
