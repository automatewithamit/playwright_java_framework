package com.playwright.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.io.File;

public class ExtentManager {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    public static void initializeExtentReports() {
        if (extent == null) {
            // Create test-output directory if it doesn't exist
            File testOutputDir = new File("test-output");
            if (!testOutputDir.exists()) {
                testOutputDir.mkdirs();
            }
            
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter("test-output/ExtentReport.html");
            sparkReporter.config().setDocumentTitle("Yatra Test Automation Report");
            sparkReporter.config().setReportName("Yatra Functional Testing");
            
            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("User", System.getProperty("user.name"));
            extent.setSystemInfo("Environment", ConfigReader.getEnvironment());
            extent.setSystemInfo("Browser", ConfigReader.getProperty("browser"));
            extent.setSystemInfo("Base URL", ConfigReader.getProperty("url"));
            
            System.out.println("ExtentReports initialized successfully");
        }
    }

    public static ExtentTest createTest(String testName) {
        if (extent == null) {
            initializeExtentReports();
        }
        ExtentTest extentTest = extent.createTest(testName);
        test.set(extentTest);
        return extentTest;
    }

    public static ExtentTest getTest() {
        return test.get();
    }

    public static void flushReports() {
        if (extent != null) {
            extent.flush();
            System.out.println("ExtentReports flushed successfully");
        }
    }
}