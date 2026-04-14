package com.playwright.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.playwright.core.PlaywrightManager;
import com.playwright.utils.ExtentManager;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Custom TestNG Listener that handles:
 * 1. Auto-creates ExtentTest per test method
 * 2. Auto-logs PASS / FAIL / SKIP status
 * 3. Auto-captures screenshot on failure and attaches to report
 */
//Why TestListener?
//1. Centralized Reporting: By implementing a TestNG listener, we can centralize the reporting logic for all our tests. This means that we can ensure consistent reporting across all test methods without having to write reporting code in each test method.
//2. Automatic Status Logging: The TestListener can automatically log the status of each test (PASS, FAIL, SKIP) without requiring manual logging in each test method. This reduces the chances of human error and ensures that all test results are accurately recorded.
//3. Screenshot Capture on Failure: The TestListener can automatically capture a screenshot whenever a test fails and attach it to the test report. This provides valuable visual context for debugging and helps identify issues more quickly.
//4. Separation of Concerns: By using a TestListener, we can separate the reporting logic from the test logic. This makes our test code cleaner and more focused on the actual testing, while the TestListener handles all the reporting-related tasks.
//5. Reusability: The TestListener can be reused across multiple test classes and projects, providing a consistent reporting mechanism without the need to duplicate reporting code in each test class. This promotes code reuse and maintainabilit

//How to implement TestListener?
//1. Create a class that implements the ITestListener interface from TestNG.
//2. Override the methods of the ITestListener interface to define the behavior for each test event (onTestStart, onTestSuccess, onTestFailure, onTestSkipped, onStart, onFinish).
//3. In the onTestStart method, create a new ExtentTest instance for the test method and log the start of the test.
//4. In the onTestSuccess method, log the test as passed in the ExtentTest instance and stop any active tracing if applicable.
//5. In the onTestFailure method, log the test as failed in the ExtentTest instance, capture a screenshot if there is an active page, and attach the screenshot to the report.
//6. In the onTestSkipped method, log the test as skipped in the ExtentTest instance and log any skip reason if available.
//7. In the onStart and onFinish methods, log the start and finish of the test suite for better visibility in the logs.
//8. Finally, register the TestListener in your TestNG XML configuration file to ensure that it is applied to your test classes.

//Difference between TestListeners and Hooks in TestNG:
//1. Purpose: TestListeners are used to listen to and respond to test events (like test start, test success, test failure, etc.) and are typically used for reporting, logging, and taking actions based on test outcomes. Hooks, on the other hand, are used to set up and tear down test environments (like @BeforeSuite, @AfterSuite, @BeforeClass, @AfterClass, @BeforeMethod, @AfterMethod) and are typically used for initializing resources, cleaning up after tests, and managing test dependencies.
//2. Scope: TestListeners operate at the level of individual test methods and can affect the reporting and logging of those methods. Hooks operate at various levels (suite, class, method) and are used to manage the lifecycle of tests and test environments.
//3. Implementation: TestListeners are implemented by creating a class that implements the ITestListener interface and overriding

public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);

    //onTestStart: This method is called when a test method starts. It creates a new ExtentTest instance for the test method and logs the start of the test.
    //onTestSuccess: This method is called when a test method passes. It logs the test as passed in the ExtentTest instance and stops any active tracing if applicable.
    //onTestFailure: This method is called when a test method fails. It logs the test as failed in the ExtentTest instance, captures a screenshot if there is an active page, and attaches the screenshot to the report.
    //onTestSkipped: This method is called when a test method is skipped. It logs the test as skipped in the ExtentTest instance and logs any skip reason if available
    //Does TestListener invoked before hooks like @BeforeMethod?
    //No, TestListeners are invoked after the hooks like @BeforeMethod. The sequence of execution in TestNG is as follows:
    //1. @BeforeSuite
    //2. @BeforeTest
    //3. @BeforeClass
    //4. @BeforeMethod
    //5. Test method execution
    //6. @AfterMethod
    //7. @AfterClass
    //8. @AfterTest
    //9. @AfterSuite
    @Override
    public void onTestStart(ITestResult result) {

        String testName = getTestName(result);
        ExtentManager.createTest(testName);
        logger.info("STARTED: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = getTestName(result);
        if (hasActivePage()) {
            PlaywrightManager.stopTracingWithoutSave();
        }
        ExtentManager.getTest().log(Status.PASS, testName + " PASSED");
        logger.info("PASSED: {}", testName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = getTestName(result);
        ExtentTest extentTest = ExtentManager.getTest();

        extentTest.log(Status.FAIL, testName + " FAILED");
        extentTest.log(Status.FAIL, result.getThrowable().getMessage());

        if (hasActivePage()) {
            PlaywrightManager.stopTracingAndSave(testName);
            captureScreenshot(testName, extentTest);
        } else {
            logger.error("FAILED: {} (API test — no screenshot)", testName);
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = getTestName(result);
        ExtentManager.createTest(testName);
        ExtentManager.getTest().log(Status.SKIP, testName + " SKIPPED");

        if (result.getThrowable() != null) {
            ExtentManager.getTest().log(Status.SKIP, result.getThrowable().getMessage());
        }

        logger.warn("SKIPPED: {}", testName);
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("\n=== Test Suite Started: " + context.getName() + " ===");
        logger.info("=== Test Suite Started: {} ===", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("=== Test Suite Finished: {} ===", context.getName());
    }

    private void captureScreenshot(String testName, ExtentTest extentTest) {
        try {
            byte[] screenshotBytes = PlaywrightManager.getPage()
                    .screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                            .setFullPage(true));

            String screenshotDir = "test-output/screenshots";
            new File(screenshotDir).mkdirs();
            String screenshotPath = screenshotDir + "/" + testName.replaceAll("[^a-zA-Z0-9]", "_") + ".png";
            java.nio.file.Files.write(Paths.get(screenshotPath), screenshotBytes);

            String base64Screenshot = Base64.getEncoder().encodeToString(screenshotBytes);
            extentTest.fail("Screenshot:",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());

            logger.error("FAILED: {} — Screenshot saved: {}", testName, screenshotPath);
        } catch (Exception e) {
            logger.error("FAILED: {} — Screenshot capture failed: {}", testName, e.getMessage());
        }
    }

    private boolean hasActivePage() {
        try {
            Page page = PlaywrightManager.getPage();
            return page != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String getTestName(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        Object[] params = result.getParameters();

        if (params != null && params.length > 0 && params[0] instanceof java.util.Map) {
            java.util.Map<?, ?> data = (java.util.Map<?, ?>) params[0];
            Object testName = data.get("testName");
            if (testName != null) {
                return methodName + " [" + testName + "]";
            }
        }

        return methodName;
    }
}
