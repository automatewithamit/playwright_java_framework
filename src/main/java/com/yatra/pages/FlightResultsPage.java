package com.yatra.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.playwright.core.BasePage;

import java.util.List;

public class FlightResultsPage extends BasePage {

    public FlightResultsPage(Page page) {
        super(page);
    }

    public boolean areFlightResultsDisplayed() {
        return page.locator("[data-cy='flight-card']").count() > 0;
    }

    public int getFlightResultsCount() {
        return page.locator("[data-cy='flight-card']").count();
    }

    public void selectFirstFlight() {
        page.locator("[data-cy='flight-card']").first().click();
    }

    public void applyPriceFilter(String minPrice, String maxPrice) {
        page.getByText("Price").click();
        page.locator("[data-cy='price-min']").fill(minPrice);
        page.locator("[data-cy='price-max']").fill(maxPrice);
        page.getByText("Apply").click();
    }

    public void sortByPrice() {
        page.getByText("Sort by").click();
        page.getByText("Price (Low to High)").click();
    }

    public String getFirstFlightPrice() {
        return page.locator("[data-cy='flight-card']").first()
                .locator("[data-cy='price']").textContent();
    }
}