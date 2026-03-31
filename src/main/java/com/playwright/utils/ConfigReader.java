package com.playwright.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Environment-aware configuration reader.
 *
 * Resolution order:
 * 1. System property override  → -Dbrowser=firefox (highest priority)
 * 2. Environment config file   → config-{env}.properties
 * 3. Default config file       → config.properties (fallback)
 *
 * Usage:
 *   mvn test                          → loads config.properties (default/dev)
 *   mvn test -Denv=staging            → loads config-staging.properties
 *   mvn test -Denv=prod               → loads config-prod.properties
 *   mvn test -Denv=staging -Dbrowser=firefox  → staging config + firefox override
 */
public class ConfigReader {
    private static Properties properties;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();

        // Determine environment from system property, default to "dev"
        String env = System.getProperty("env", "dev");
        String configFile = "config-" + env + ".properties";

        // Try environment-specific config first, fallback to default
        InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream(configFile);

        if (inputStream == null) {
            System.out.println("Config file '" + configFile + "' not found, falling back to config.properties");
            inputStream = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties");
        } else {
            System.out.println("Loaded environment config: " + configFile);
        }

        if (inputStream == null) {
            throw new RuntimeException("No configuration file found in classpath");
        }

        try (InputStream is = inputStream) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load config file: " + e.getMessage());
        }
    }

    /**
     * Gets a property value. System properties (-D flags) take highest priority,
     * then environment config file, then default config.
     */
    public static String getProperty(String key) {
        // System property override takes priority
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }

        String value = properties.getProperty(key);
        if (value == null) {
            throw new RuntimeException("Property '" + key + "' not found in config");
        }
        return value;
    }

    public static String getProperty(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (systemValue != null) {
            return systemValue;
        }
        return properties.getProperty(key, defaultValue);
    }

    public static String getEnvironment() {
        return System.getProperty("env", "dev");
    }
}
