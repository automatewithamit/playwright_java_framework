package com.playwright.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 */
public class ConfigReader {

    private static final Logger logger = LogManager.getLogger(ConfigReader.class);
    private static Properties properties;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        properties = new Properties();

        String env = System.getProperty("env", "dev");
        String configFile = "config-" + env + ".properties";

        InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream(configFile);

        if (inputStream == null) {
            logger.warn("Config file '{}' not found, falling back to config.properties", configFile);
            inputStream = ConfigReader.class.getClassLoader().getResourceAsStream("config.properties");
        } else {
            logger.info("Loaded environment config: {}", configFile);
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

    public static String getProperty(String key) {
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
