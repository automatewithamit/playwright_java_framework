package com.yatra.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.playwright.core.BasePage;

public class LoginPopupPage extends BasePage {

    public LoginPopupPage(Page page) {
        super(page);
    }

    public void closeLoginPopUp(){
        Locator popup = page.locator("section")
                .filter(new Locator.FilterOptions()
                        .setHasText("Email Id / Mobile Number"));

        popup.getByAltText("cross").click();
        System.out.println("Closed login popup successfully");
    }
}