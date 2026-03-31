package com.playwright.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.playwright.core.WebDriverManager;
import com.playwright.utils.ExtentManager;
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
 *
 * With this listener, test classes no longer need:
 * - ExtentManager.createTest()
 * - try/catch blocks for reporting
 * - Manual Status.PASS / Status.FAIL logging
 */
public class TestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);
        ExtentManager.createTest(testName);
        System.out.println("[" + Thread.currentThread().getName() + "] STARTED: " + testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = getTestName(result);
        ExtentManager.getTest().log(Status.PASS, testName + " PASSED");
        System.out.println("[" + Thread.currentThread().getName() + "] PASSED: " + testName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = getTestName(result);
        ExtentTest extentTest = ExtentManager.getTest();

        // Log failure message
        extentTest.log(Status.FAIL, testName + " FAILED");
        extentTest.log(Status.FAIL, result.getThrowable().getMessage());

        // Capture and attach screenshot
        try {
            byte[] screenshotBytes = WebDriverManager.getPage()
                    .screenshot(new com.microsoft.playwright.Page.ScreenshotOptions()
                            .setFullPage(true));

            // Save to file
            String screenshotDir = "test-output/screenshots";
            new File(screenshotDir).mkdirs();
            String screenshotPath = screenshotDir + "/" + testName.replaceAll("[^a-zA-Z0-9]", "_") + ".png";
            java.nio.file.Files.write(Paths.get(screenshotPath), screenshotBytes);

            // Attach to ExtentReport as base64
            String base64Screenshot = Base64.getEncoder().encodeToString(screenshotBytes);
            extentTest.fail("Screenshot:",
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());

            System.out.println("[" + Thread.currentThread().getName() + "] Screenshot saved: " + screenshotPath);
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot: " + e.getMessage());
        }

        System.out.println("[" + Thread.currentThread().getName() + "] FAILED: " + testName);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = getTestName(result);
        ExtentManager.createTest(testName);
        ExtentManager.getTest().log(Status.SKIP, testName + " SKIPPED");

        if (result.getThrowable() != null) {
            ExtentManager.getTest().log(Status.SKIP, result.getThrowable().getMessage());
        }

        System.out.println("[" + Thread.currentThread().getName() + "] SKIPPED: " + testName);
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("=== Test Suite Started: " + context.getName() + " ===");
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("=== Test Suite Finished: " + context.getName() + " ===");
    }

    /**
     * Builds a descriptive test name.
     * For DataProvider tests: "testFlightSearch [Delhi to Mumbai Flight Search]"
     * For regular tests: "testHotelSearch"
     */
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
