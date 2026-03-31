package com.playwright.core;

import com.microsoft.playwright.*;
import com.playwright.utils.ConfigReader;
import java.util.Locale;

/**
 * Thread-safe browser management using Playwright's BrowserContext isolation.
 *
 * Architecture:
 *   Playwright + Browser  → shared, launched once per suite
 *   BrowserContext + Page  → isolated per thread/test via ThreadLocal
 *
 * Each BrowserContext has its own cookies, localStorage, and session —
 * no state leaks between parallel tests.
 */
public class WebDriverManager {

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
            return; // already launched
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
                System.out.println("Browser not supported: " + browserName + ". Using Chrome as default.");
                browser = playwright.chromium().launch(launchOptions);
        }

        System.out.println("Browser launched: " + browserName);
    }

    /**
     * Creates a new isolated BrowserContext and Page for the current thread.
     * Call this in @BeforeMethod.
     */
    public static void createContext() {
        if (browser == null) {
            throw new RuntimeException("Browser not launched. Call launchBrowser() first.");
        }

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        contextThreadLocal.set(context);

        Page page = context.newPage();
        pageThreadLocal.set(page);

        System.out.println("[" + Thread.currentThread().getName() + "] New context created");
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
        System.out.println("[" + Thread.currentThread().getName() + "] Navigated to: " + url);
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

            System.out.println("[" + Thread.currentThread().getName() + "] Context closed");
        } catch (Exception e) {
            System.err.println("[" + Thread.currentThread().getName() + "] Error closing context: " + e.getMessage());
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
            System.out.println("Browser and Playwright shut down");
        } catch (Exception e) {
            System.err.println("Error shutting down browser: " + e.getMessage());
        }
    }
}
