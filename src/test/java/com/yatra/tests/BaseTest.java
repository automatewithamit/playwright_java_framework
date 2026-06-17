package com.yatra.tests;

import com.playwright.core.PlaywrightManager;
import com.playwright.listeners.TestListener;
import com.playwright.utils.ConfigReader;
import com.playwright.utils.ExtentManager;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

@Listeners(TestListener.class)
public class BaseTest {

    @BeforeSuite
    public void suiteSetup() {
        ExtentManager.initializeExtentReports();
        String browserName = System.getProperty("brwoser");
        PlaywrightManager.launchBrowser(browserName);
    }

    @BeforeMethod
    public void setUp() {
        PlaywrightManager.createContext();
        System.getProperty("headless", "false");
        PlaywrightManager.navigateTo(ConfigReader.getProperty("url"));
    }

    @AfterMethod
    public void tearDown() {
        PlaywrightManager.closeContext();
    }

    @AfterSuite
    public void suiteTearDown() {
        PlaywrightManager.quitBrowser();
        ExtentManager.flushReports();
    }
}
