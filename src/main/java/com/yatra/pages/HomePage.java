package com.yatra.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.core.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

public class HomePage extends BasePage {

    private static final Logger logger = LogManager.getLogger(HomePage.class);

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
        logger.info("Selected tab: {}", tabName);
    }

    public void closeLoginPopupIfPresent() {
        try {
            if (page.locator("section").filter(new Locator.FilterOptions()
                    .setHasText("Email Id / Mobile Number")).isVisible()) {
                page.locator("section")
                        .filter(new Locator.FilterOptions()
                                .setHasText("Email Id / Mobile Number"))
                        .getByAltText("cross").click();
                logger.info("Closed login popup");
            }
        } catch (Exception e) {
            logger.debug("No login popup found: {}", e.getMessage());
        }
    }

    public FlightSearchPage getFlightSearchPage() {
        return new FlightSearchPage(page);
    }
}
