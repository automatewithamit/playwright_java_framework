package com.yatra.tests;

import com.playwright.core.WebDriverManager;
import com.playwright.utils.ConfigReader;
import com.playwright.utils.ExtentManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

public class BaseTest {
    
    @BeforeSuite
    public void suiteSetup() {
        ExtentManager.initializeExtentReports();
    }

    @BeforeMethod
    public void setUp() {
        String browser = ConfigReader.getProperty("browser");
        String url = ConfigReader.getProperty("url");
        
        WebDriverManager.initializeBrowser(browser);
        WebDriverManager.navigateTo(url);
    }

    @AfterMethod
    public void tearDown() {
        WebDriverManager.closeBrowser();
    }

    @AfterSuite
    public void suiteTearDown() {
        ExtentManager.flushReports();
    }
}