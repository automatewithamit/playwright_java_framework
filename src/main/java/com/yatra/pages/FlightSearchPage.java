package com.yatra.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.core.BasePage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FlightSearchPage extends BasePage {

    private static final Logger logger = LogManager.getLogger(FlightSearchPage.class);

    public FlightSearchPage(Page page) {
        super(page);
    }

    public void enterDepartureCity(String departureCity) {
        if (departureCity == null || departureCity.trim().isEmpty()) {
            throw new IllegalArgumentException("Departure city cannot be null or empty");
        }
        setComboBoxText("Departure From New Delhi inputbox", departureCity);
        logger.info("Entered departure city: {}", departureCity);
    }

    public void enterArrivalCity(String arrivalCity) {
        if (arrivalCity == null || arrivalCity.trim().isEmpty()) {
            throw new IllegalArgumentException("Arrival city cannot be null or empty");
        }
        setComboBoxText("Going to Goa inputbox", arrivalCity);
        logger.info("Entered arrival city: {}", arrivalCity);
    }

    public void selectDepartureDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty");
        }
        page.getByLabel("Departure Date").click();
        page.locator("[data-date='" + date + "']").click();
        logger.info("Selected departure date: {}", date);
    }

    public void selectReturnDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty");
        }
        page.getByLabel("Return Date").click();
        page.locator("[data-date='" + date + "']").click();
        logger.info("Selected return date: {}", date);
    }

    public void selectPassengers(int adults, int children, int infants) {
        if (adults < 1) {
            throw new IllegalArgumentException("At least 1 adult passenger is required");
        }
        if (children < 0 || infants < 0) {
            throw new IllegalArgumentException("Children and infants count cannot be negative");
        }

        page.getByText("Travellers & Class").click();

        for (int i = 1; i < adults; i++) {
            page.locator("[data-cy='adults-plus']").click();
        }
        for (int i = 0; i < children; i++) {
            page.locator("[data-cy='children-plus']").click();
        }
        for (int i = 0; i < infants; i++) {
            page.locator("[data-cy='infants-plus']").click();
        }

        page.getByText("Done").click();
        logger.info("Selected passengers — Adults: {}, Children: {}, Infants: {}", adults, children, infants);
    }

    public void clickSearchFlights() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search Flights")).click();
        logger.info("Clicked Search Flights");
    }

    private void setComboBoxText(String comboBoxName, String text) {
        Locator combo = page.getByLabel(comboBoxName);
        combo.fill(text);
        page.waitForTimeout(1000);
        page.keyboard().press("ArrowDown");
        page.keyboard().press("Enter");
    }
}
