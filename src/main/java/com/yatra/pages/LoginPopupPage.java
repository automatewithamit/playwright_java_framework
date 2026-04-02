package com.yatra.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.playwright.core.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoginPopupPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(LoginPopupPage.class);

    public LoginPopupPage(Page page) {
        super(page);
    }

    public void closeLoginPopUp() {
        Locator popup = page.locator("section")
                .filter(new Locator.FilterOptions()
                        .setHasText("Email Id / Mobile Number"));

        popup.getByAltText("cross").click();
        logger.info("Closed login popup");
    }
}
