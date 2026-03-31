package com.yatra.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.playwright.core.BasePage;

public class FlightSearchPage extends BasePage {

    public FlightSearchPage(Page page) {
        super(page);
    }

    public void enterDepartureCity(String departureCity){
        if (departureCity == null || departureCity.trim().isEmpty()) {
            throw new IllegalArgumentException("Departure city cannot be null or empty");
        }
        try {
            setComboBoxText("Departure From New Delhi inputbox", departureCity);
            System.out.println("Entered departure city: " + departureCity);
        } catch (Exception e) {
            System.err.println("Failed to enter departure city: " + e.getMessage());
            throw e;
        }
    }

    public void enterArrivalCity(String arrivalCity){
        if (arrivalCity == null || arrivalCity.trim().isEmpty()) {
            throw new IllegalArgumentException("Arrival city cannot be null or empty");
        }
        try {
            setComboBoxText("Going to Goa inputbox", arrivalCity);
            System.out.println("Entered arrival city: " + arrivalCity);
        } catch (Exception e) {
            System.err.println("Failed to enter arrival city: " + e.getMessage());
            throw e;
        }
    }

    public void selectDepartureDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty");
        }
        try {
            page.getByLabel("Departure Date").click();
            page.locator("[data-date='" + date + "']").click();
            System.out.println("Selected departure date: " + date);
        } catch (Exception e) {
            System.err.println("Failed to select departure date: " + e.getMessage());
            throw e;
        }
    }

    public void selectReturnDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            throw new IllegalArgumentException("Date cannot be null or empty");
        }
        try {
            page.getByLabel("Return Date").click();
            page.locator("[data-date='" + date + "']").click();
            System.out.println("Selected return date: " + date);
        } catch (Exception e) {
            System.err.println("Failed to select return date: " + e.getMessage());
            throw e;
        }
    }

    public void selectPassengers(int adults, int children, int infants) {
        if (adults < 1) {
            throw new IllegalArgumentException("At least 1 adult passenger is required");
        }
        if (children < 0 || infants < 0) {
            throw new IllegalArgumentException("Children and infants count cannot be negative");
        }
        
        try {
            page.getByText("Travellers & Class").click();
            
            // Set adults (default is 1, so add additional)
            for (int i = 1; i < adults; i++) {
                page.locator("[data-cy='adults-plus']").click();
            }
            
            // Set children
            for (int i = 0; i < children; i++) {
                page.locator("[data-cy='children-plus']").click();
            }
            
            // Set infants
            for (int i = 0; i < infants; i++) {
                page.locator("[data-cy='infants-plus']").click();
            }
            
            page.getByText("Done").click();
            System.out.println("Selected passengers - Adults: " + adults + ", Children: " + children + ", Infants: " + infants);
        } catch (Exception e) {
            System.err.println("Failed to select passengers: " + e.getMessage());
            throw e;
        }
    }

    public void clickSearchFlights() {
        try {
            page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Search Flights")).click();
            System.out.println("Clicked search flights button");
        } catch (Exception e) {
            System.err.println("Failed to click search flights button: " + e.getMessage());
            throw e;
        }
    }

    private void setComboBoxText(String comboBoxName, String text){
        try {
            Locator combo = page.getByLabel(comboBoxName);
            combo.fill(text);
            page.waitForTimeout(1000);
            page.keyboard().press("ArrowDown");
            page.keyboard().press("Enter");
        } catch (Exception e) {
            System.err.println("Failed to set combo box text for " + comboBoxName + ": " + e.getMessage());
            throw e;
        }
    }
}