package com.cluedrive.commons;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertiesUtility {
    public static String applicationProperty(String key) {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(
                Paths.get("cluedrive-application/build/resources/main/config.properties").toAbsolutePath().toFile())) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }

    public static String apiProperty(String key) {
        Properties properties = new Properties();
        try (InputStream inputStream = new FileInputStream(
                Paths.get("cluedrive-api/build/resources/main/config.properties").toAbsolutePath().toFile())) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties.getProperty(key);
    }
}
