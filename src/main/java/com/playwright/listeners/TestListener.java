package com.playwright.listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.playwright.core.WebDriverManager;
import com.playwright.utils.ExtentManager;
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
public class TestListener implements ITestListener {

    private static final Logger logger = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestStart(ITestResult result) {
        String testName = getTestName(result);
        ExtentManager.createTest(testName);
        logger.info("STARTED: {}", testName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = getTestName(result);
        WebDriverManager.stopTracingWithoutSave();
        ExtentManager.getTest().log(Status.PASS, testName + " PASSED");
        logger.info("PASSED: {}", testName);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testName = getTestName(result);
        ExtentTest extentTest = ExtentManager.getTest();

        extentTest.log(Status.FAIL, testName + " FAILED");
        extentTest.log(Status.FAIL, result.getThrowable().getMessage());

        WebDriverManager.stopTracingAndSave(testName);

        try {
            byte[] screenshotBytes = WebDriverManager.getPage()
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
        logger.info("=== Test Suite Started: {} ===", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        logger.info("=== Test Suite Finished: {} ===", context.getName());
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
