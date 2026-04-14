package com.yatra.tests.api;

import com.playwright.core.api.ApiRequestManager;
import com.playwright.listeners.TestListener;
import com.playwright.utils.ConfigReader;
import com.playwright.utils.ExtentManager;
import org.testng.annotations.*;

/**
 * Base class for all API tests.
 * Manages Playwright + APIRequestContext lifecycle.
 *
 * Lifecycle:
 *   @BeforeSuite  → Playwright.create() + ExtentReports init
 *   @BeforeMethod → new APIRequestContext per thread
 *   @AfterMethod  → dispose context
 *   @AfterSuite   → Playwright.close() + flush reports
 */
@Listeners(TestListener.class)
public class BaseApiTest {

    @BeforeSuite
    public void suiteSetup() {
        ExtentManager.initializeExtentReports();
        ApiRequestManager.initPlaywright();
    }

    @BeforeMethod
    public void setUp() {
        ApiRequestManager.createRequestContext();
    }

    @AfterMethod
    public void tearDown() {
        ApiRequestManager.closeRequestContext();
    }

    @AfterSuite
    public void suiteTearDown() {
        ApiRequestManager.closePlaywright();
        ExtentManager.flushReports();
    }
}
