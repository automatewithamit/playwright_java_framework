package com.yatra.tests;

import com.playwright.core.WebDriverManager;
import com.playwright.utils.ConfigReader;
import org.testng.annotations.Test;

public class FrameworkValidationTest {

    @Test
    public void validateFrameworkSetup() {
        try {
            // Test ConfigReader
            String browser = ConfigReader.getProperty("browser");
            String url = ConfigReader.getProperty("url");
            System.out.println("Configuration loaded - Browser: " + browser + ", URL: " + url);
            
            // Test WebDriverManager
            WebDriverManager.initializeBrowser(browser);
            System.out.println("Browser initialized successfully");
            
            // Test navigation
            WebDriverManager.navigateTo(url);
            System.out.println("Navigation successful");
            
            // Test page title
            String title = WebDriverManager.getPage().title();
            System.out.println("Page title: " + title);
            
            // Close browser
            WebDriverManager.closeBrowser();
            System.out.println("Framework validation completed successfully");
            
        } catch (Exception e) {
            System.err.println("Framework validation failed: " + e.getMessage());
            throw e;
        }
    }
}