package com.playwright.core;

import com.microsoft.playwright.*;
import com.microsoft.playwright.Tracing;
import com.playwright.utils.ConfigReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

/**
 * Thread-safe browser management using Playwright's BrowserContext isolation.
 *
 * Architecture:
 *   Playwright + Browser  → shared, launched once per suite
 *   BrowserContext + Page  → isolated per thread/test via ThreadLocal
 */
public class WebDriverManager {

    private static final Logger logger = LogManager.getLogger(WebDriverManager.class);

    // Shared across all threads — launched once
    private static Playwright playwright;
    private static Browser browser;

    // Isolated per thread — created per test
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    /**
     * Launches Playwright and Browser once for the entire suite.
     * Call this in @BeforeSuite.
     */
    public static synchronized void launchBrowser(String browserName) {
        if (browser != null) {
            return;
        }

        if (browserName == null || browserName.trim().isEmpty()) {
            browserName = "chrome";
        }

        playwright = Playwright.create();

        boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless", "false"));

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(500);

        switch (browserName.toLowerCase(Locale.ROOT)) {
            case "chrome":
            case "chromium":
                browser = playwright.chromium().launch(launchOptions);
                break;
            case "firefox":
                browser = playwright.firefox().launch(launchOptions);
                break;
            case "safari":
            case "webkit":
                browser = playwright.webkit().launch(launchOptions);
                break;
            default:
                logger.warn("Browser not supported: {}. Using Chrome as default.", browserName);
                browser = playwright.chromium().launch(launchOptions);
        }

        logger.info("Browser launched: {} (headless={})", browserName, headless);
    }

    /**
     * Creates a new isolated BrowserContext and Page for the current thread.
     * Starts tracing if enabled in config.
     * Call this in @BeforeMethod.
     */
    public static void createContext() {
        if (browser == null) {
            throw new RuntimeException("Browser not launched. Call launchBrowser() first.");
        }

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        contextThreadLocal.set(context);

        if (Boolean.parseBoolean(ConfigReader.getProperty("tracing", "false"))) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(false));
        }

        Page page = context.newPage();
        pageThreadLocal.set(page);

        logger.debug("New context created (tracing={})",
                ConfigReader.getProperty("tracing", "false"));
    }

    public static Page getPage() {
        Page page = pageThreadLocal.get();
        if (page == null) {
            throw new RuntimeException("Context not created. Call createContext() first.");
        }
        return page;
    }

    public static BrowserContext getContext() {
        return contextThreadLocal.get();
    }

    public static void navigateTo(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        getPage().navigate(url);
        logger.info("Navigated to: {}", url);
    }

    /**
     * Stops tracing and saves the trace file for the given test.
     * Call this from TestListener on failure, or whenever you want to save a trace.
     */
    public static void stopTracingAndSave(String testName) {
        try {
            BrowserContext context = contextThreadLocal.get();
            if (context != null && Boolean.parseBoolean(ConfigReader.getProperty("tracing", "false"))) {
                String safeName = testName.replaceAll("[^a-zA-Z0-9]", "_");
                Path tracePath = Paths.get("test-output", "traces", safeName + ".zip");
                tracePath.getParent().toFile().mkdirs();
                context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                logger.info("Trace saved: {}", tracePath);
            }
        } catch (Exception e) {
            logger.error("Failed to save trace: {}", e.getMessage());
        }
    }

    /**
     * Stops tracing without saving (for passed tests when tracing.onFailureOnly=true).
     */
    public static void stopTracingWithoutSave() {
        try {
            BrowserContext context = contextThreadLocal.get();
            if (context != null && Boolean.parseBoolean(ConfigReader.getProperty("tracing", "false"))) {
                context.tracing().stop();
            }
        } catch (Exception e) {
            logger.debug("Tracing stop (no save): {}", e.getMessage());
        }
    }

    /**
     * Closes the current thread's BrowserContext and Page.
     * Call this in @AfterMethod.
     */
    public static void closeContext() {
        try {
            Page page = pageThreadLocal.get();
            if (page != null) {
                page.close();
                pageThreadLocal.remove();
            }

            BrowserContext context = contextThreadLocal.get();
            if (context != null) {
                context.close();
                contextThreadLocal.remove();
            }

            logger.debug("Context closed");
        } catch (Exception e) {
            logger.error("Error closing context: {}", e.getMessage(), e);
        }
    }

    /**
     * Shuts down the shared Browser and Playwright instance.
     * Call this in @AfterSuite.
     */
    public static synchronized void quitBrowser() {
        try {
            if (browser != null) {
                browser.close();
                browser = null;
            }
            if (playwright != null) {
                playwright.close();
                playwright = null;
            }
            logger.info("Browser and Playwright shut down");
        } catch (Exception e) {
            logger.error("Error shutting down browser: {}", e.getMessage(), e);
        }
    }
}
