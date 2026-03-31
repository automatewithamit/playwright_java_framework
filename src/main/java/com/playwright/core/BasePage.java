package com.playwright.core;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

public abstract class BasePage {
    protected final Page page;

    public BasePage(Page page) {
        this.page = page;
    }

    protected void click(String selector) {
        page.locator(selector).click();
    }

    protected void fill(String selector, String text) {
        page.locator(selector).fill(text);
    }

    protected void waitForSelector(String selector) {
        page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                .setState(WaitForSelectorState.VISIBLE));
    }

    protected String getText(String selector) {
        return page.locator(selector).textContent();
    }

    protected boolean isVisible(String selector) {
        return page.locator(selector).isVisible();
    }

    protected void selectOption(String selector, String value) {
        page.locator(selector).selectOption(value);
    }

    protected void waitForPageLoad() {
        page.waitForLoadState();
    }
}