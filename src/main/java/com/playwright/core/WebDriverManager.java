package com.playwright.core;

import com.microsoft.playwright.*;
import com.playwright.utils.ConfigReader;
import java.util.Locale;

public class WebDriverManager {
    private static Playwright playwright;
    private static Browser browser;
    private static BrowserContext context;
    private static Page page;

    public static void initializeBrowser(String browserName) {
        if (browserName == null || browserName.trim().isEmpty()) {
            browserName = "chrome"; // default browser
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

        context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1920, 1080));
        page = context.newPage();
        
        System.out.println("Browser initialized: " + browserName);
    }

    public static Page getPage() {
        if (page == null) {
            throw new RuntimeException("Browser not initialized. Call initializeBrowser() first.");
        }
        return page;
    }

    public static void navigateTo(String url) {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        getPage().navigate(url);
        System.out.println("Navigated to: " + url);
    }

    public static void closeBrowser() {
        try {
            if (page != null) {
                page.close();
                page = null;
            }
            if (context != null) {
                context.close();
                context = null;
            }
            if (browser != null) {
                browser.close();
                browser = null;
            }
            if (playwright != null) {
                playwright.close();
                playwright = null;
            }
            System.out.println("Browser closed successfully");
        } catch (Exception e) {
            System.err.println("Error closing browser: " + e.getMessage());
        }
    }
}