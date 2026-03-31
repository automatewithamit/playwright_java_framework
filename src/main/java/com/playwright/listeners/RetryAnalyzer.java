package com.playwright.listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Auto-retries failed tests up to MAX_RETRY times.
 * Helps handle flaky tests caused by network issues, timing, etc.
 *
 * Applied automatically to all tests via RetryTransformer.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final int MAX_RETRY = 1;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            System.out.println("[RETRY] " + result.getMethod().getMethodName()
                    + " - Attempt " + (retryCount + 1) + " of " + (MAX_RETRY + 1));
            return true;
        }
        return false;
    }
}
