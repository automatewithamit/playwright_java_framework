package com.yatra.tests;

import com.playwright.core.WebDriverManager;
import com.playwright.utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FrameworkValidationTest extends BaseTest {

    @Test
    public void validateFrameworkSetup() {
        // BaseTest already launched browser, created context, and navigated to URL

        String title = WebDriverManager.getPage().title();
        System.out.println("Page title: " + title);

        String currentUrl = WebDriverManager.getPage().url();
        System.out.println("Current URL: " + currentUrl);

        Assert.assertNotNull(title, "Page title should not be null");
        Assert.assertTrue(currentUrl.contains("yatra"), "URL should contain 'yatra'");

        System.out.println("Framework validation completed successfully");
    }
}
