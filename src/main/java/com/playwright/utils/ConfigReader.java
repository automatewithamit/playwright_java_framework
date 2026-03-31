package com.playwright.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {
    private static Properties properties;
    private static final String CONFIG_FILE_PATH = "config.properties";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();
        try (InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream(CONFIG_FILE_PATH)) {
            if (inputStream == null) {
                throw new RuntimeException("config.properties file not found in classpath");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load config.properties file: " + e.getMessage());
        }
    }

    public static String getProperty(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in config.properties");
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
}