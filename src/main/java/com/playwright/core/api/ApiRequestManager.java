package com.playwright.core.api;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import com.playwright.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Thread-safe API request context manager using Playwright's APIRequestContext.
 *
 * Architecture (mirrors WebDriverManager):
 *   Playwright          → shared, created once per suite
 *   APIRequestContext    → isolated per thread via ThreadLocal
 */
public class ApiRequestManager {

    private static final Logger logger = LogManager.getLogger(ApiRequestManager.class);

    private static Playwright playwright;
    private static final ThreadLocal<APIRequestContext> requestContextThreadLocal = new ThreadLocal<>();

    public static synchronized void initPlaywright() {
        if (playwright == null) {
            playwright = Playwright.create();
            logger.info("Playwright initialized for API testing");
        }
    }

    /**
     * Creates a new APIRequestContext for the current thread with the configured base URL.
     * Call this in @BeforeMethod.
     */
    public static void createRequestContext() {
        createRequestContext(ConfigReader.getProperty("api.base.url", ""));
    }

    public static void createRequestContext(String baseURL) {
        if (playwright == null) {
            throw new RuntimeException("Playwright not initialized. Call initPlaywright() first.");
        }

        APIRequest.NewContextOptions options = new APIRequest.NewContextOptions();
        if (baseURL != null && !baseURL.trim().isEmpty()) {
            options.setBaseURL(baseURL);
        }

        APIRequestContext context = playwright.request().newContext(options);
        requestContextThreadLocal.set(context);
        logger.debug("API request context created (baseURL={})", baseURL);
    }

    /**
     * Creates a request context with custom headers (e.g., auth tokens).
     */
    public static void createRequestContext(String baseURL, Map<String, String> headers) {
        if (playwright == null) {
            throw new RuntimeException("Playwright not initialized. Call initPlaywright() first.");
        }

        APIRequest.NewContextOptions options = new APIRequest.NewContextOptions();
        if (baseURL != null && !baseURL.trim().isEmpty()) {
            options.setBaseURL(baseURL);
        }
        if (headers != null && !headers.isEmpty()) {
            options.setExtraHTTPHeaders(headers);
        }

        APIRequestContext context = playwright.request().newContext(options);
        requestContextThreadLocal.set(context);
        logger.debug("API request context created with custom headers (baseURL={})", baseURL);
    }

    public static APIRequestContext getRequestContext() {
        APIRequestContext context = requestContextThreadLocal.get();
        if (context == null) {
            throw new RuntimeException("Request context not created. Call createRequestContext() first.");
        }
        return context;
    }

    public static void closeRequestContext() {
        try {
            APIRequestContext context = requestContextThreadLocal.get();
            if (context != null) {
                context.dispose();
                requestContextThreadLocal.remove();
            }
            logger.debug("API request context closed");
        } catch (Exception e) {
            logger.error("Error closing API request context: {}", e.getMessage(), e);
        }
    }

    public static synchronized void closePlaywright() {
        try {
            if (playwright != null) {
                playwright.close();
                playwright = null;
            }
            logger.info("Playwright shut down (API)");
        } catch (Exception e) {
            logger.error("Error shutting down Playwright: {}", e.getMessage(), e);
        }
    }
}
