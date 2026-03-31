package com.playwright.core;

import com.microsoft.playwright.*;
import com.playwright.utils.ConfigReader;
import java.util.Locale;

/**
 * Thread-safe browser management using ThreadLocal.
 * Each test thread gets its own Playwright → Browser → Context → Page chain.
 * This enables safe parallel test execution.
 */
public class WebDriverManager {

    private static final ThreadLocal<Playwright> playwrightThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Browser> browserThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> contextThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<Page> pageThreadLocal = new ThreadLocal<>();

    public static void initializeBrowser(String browserName) {
        if (browserName == null || browserName.trim().isEmpty()) {
            browserName = "chrome";
        }

        Playwright playwright = Playwright.create();
        playwrightThreadLocal.set(playwright);

        boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless", "false"));

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(500);

        Browser browser;
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
        browserThreadLocal.set(browser);

        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        contextThreadLocal.set(context);

        Page page = context.newPage();
        pageThreadLocal.set(page);

        System.out.println("[" + Thread.currentThread().getName() + "] Browser initialized: " + browserName);
    }

    public static Page getPage() {
        Page page = pageThreadLocal.get();
        if (page == null) {
            throw new RuntimeException("Browser not initialized. Call initializeBrowser() first.");
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

    public static void closeBrowser() {
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

            Browser browser = browserThreadLocal.get();
            if (browser != null) {
                browser.close();
                browserThreadLocal.remove();
            }

            Playwright playwright = playwrightThreadLocal.get();
            if (playwright != null) {
                playwright.close();
                playwrightThreadLocal.remove();
            }

            System.out.println("[" + Thread.currentThread().getName() + "] Browser closed successfully");
        } catch (Exception e) {
            System.err.println("[" + Thread.currentThread().getName() + "] Error closing browser: " + e.getMessage());
        }
    }
}
