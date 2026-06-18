package com.yatra.tests;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
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
    Playwright playwright;
    BrowserContext context;
    @BeforeSuite
    public void suiteSetup() {
         playwright = Playwright.create();
//        ExtentManager.initializeExtentReports();
//        String browserName = System.getProperty("brwoser");
//        PlaywrightManager.launchBrowser(browserName);
    }

    @BeforeMethod
    public void setUp() {

         context = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(false)).newContext();
         context.newPage().navigate("https://yatra.com");
//        PlaywrightManager.createContext();
//        System.getProperty("headless", "false");
//        PlaywrightManager.navigateTo(ConfigReader.getProperty("url"));
    }

    @AfterMethod
    public void tearDown() {
//        PlaywrightManager.closeContext();

        context.close();
    }

    @AfterSuite
    public void suiteTearDown() {
        playwright.close();
//        PlaywrightManager.quitBrowser();
//        ExtentManager.flushReports();
    }
}
