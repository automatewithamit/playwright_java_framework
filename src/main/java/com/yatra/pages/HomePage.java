package com.yatra.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.core.BasePage;

import java.util.regex.Pattern;

public class HomePage extends BasePage {

    public HomePage(Page page) {
        super(page);
    }

    private Locator mainTabList() {
        return page.getByRole(
                AriaRole.TABLIST,
                new Page.GetByRoleOptions().setName("Tab navigation")
        );
    }

    public void selectTab(String tabName) {
        if (tabName == null || tabName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tab name cannot be null or empty");
        }
        
        String name = "^" + tabName + ",";
        mainTabList()
                .getByRole(
                        AriaRole.TAB,
                        new Locator.GetByRoleOptions()
                                    .setName(Pattern.compile(name, Pattern.CASE_INSENSITIVE))
                ).click();
        System.out.println("Successfully clicked on tab: " + tabName);
    }

    public void closeLoginPopupIfPresent() {
        try {
            if (page.locator("section").filter(new Locator.FilterOptions()
                    .setHasText("Email Id / Mobile Number")).isVisible()) {
                page.locator("section")
                        .filter(new Locator.FilterOptions()
                                .setHasText("Email Id / Mobile Number"))
                        .getByAltText("cross").click();
                System.out.println("Closed login popup successfully");
            }
        } catch (Exception e) {
            System.out.println("No login popup found or unable to close: " + e.getMessage());
        }
    }

    public FlightSearchPage getFlightSearchPage() {
        return new FlightSearchPage(page);
    }
}