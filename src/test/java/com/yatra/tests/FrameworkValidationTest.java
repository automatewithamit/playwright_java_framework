package com.yatra.tests;

import com.playwright.core.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FrameworkValidationTest extends BaseTest {

    private static final Logger logger = LogManager.getLogger(FrameworkValidationTest.class);

    @Test
    public void validateFrameworkSetup() {
        String title = WebDriverManager.getPage().title();
        String currentUrl = WebDriverManager.getPage().url();

        logger.info("Page title: {}", title);
        logger.info("Current URL: {}", currentUrl);

        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertTrue(currentUrl.contains("yatra"), "URL should contain 'yatra'");

        logger.info("Framework validation completed successfully");
    }
}
