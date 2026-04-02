package com.playwright.listeners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Auto-retries failed tests up to MAX_RETRY times.
 * Applied automatically to all tests via RetryTransformer.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger logger = LogManager.getLogger(RetryAnalyzer.class);
    private static final int MAX_RETRY = 1;
    private int retryCount = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < MAX_RETRY) {
            retryCount++;
            logger.warn("RETRY: {} — Attempt {} of {}",
                    result.getMethod().getMethodName(), retryCount + 1, MAX_RETRY + 1);
            return true;
        }
        return false;
    }
}
