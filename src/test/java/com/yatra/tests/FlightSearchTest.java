package com.yatra.tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.PlaywrightException;
import com.playwright.core.WebDriverManager;
import com.playwright.utils.ExtentManager;
import com.yatra.pages.FlightResultsPage;
import com.yatra.pages.FlightSearchPage;
import com.yatra.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FlightSearchTest extends BaseTest {

    @Test(priority = 1)
    public void testFlightSearch() {
        ExtentTest test = ExtentManager.createTest("Flight Search Test");
        
        try {
            HomePage homePage = new HomePage(WebDriverManager.getPage());
            test.log(Status.INFO, "Navigated to Yatra homepage");
            
            // Close login popup if present
            homePage.closeLoginPopupIfPresent();
            test.log(Status.INFO, "Closed login popup if present");
            
            // Select Flights tab
            homePage.selectTab("Flights");
            test.log(Status.INFO, "Selected Flights tab");
            
            FlightSearchPage flightSearchPage = homePage.getFlightSearchPage();
            
            // Enter flight search details
            flightSearchPage.enterDepartureCity("Delhi");
            test.log(Status.INFO, "Entered departure city: Delhi");
            
            flightSearchPage.enterArrivalCity("Mumbai");
            test.log(Status.INFO, "Entered arrival city: Mumbai");
            
            // Select passengers
            flightSearchPage.selectPassengers(2, 0, 0);
            test.log(Status.INFO, "Selected 2 adult passengers");
            
            // Search flights
            flightSearchPage.clickSearchFlights();
            test.log(Status.INFO, "Clicked search flights button");
            
            // Wait for results page
            WebDriverManager.getPage().waitForTimeout(5000);
            
            FlightResultsPage resultsPage = new FlightResultsPage(WebDriverManager.getPage());
            
            // Verify results are displayed
            Assert.assertTrue(resultsPage.areFlightResultsDisplayed(), 
                "Flight results should be displayed");
            test.log(Status.PASS, "Flight search completed successfully");
            
        } catch (PlaywrightException e) {
            test.log(Status.FAIL, "Playwright error: " + e.getMessage());
            Assert.fail("Playwright error: " + e.getMessage());
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Assertion failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 2)
    public void testHotelSearch() {
        ExtentTest test = ExtentManager.createTest("Hotel Search Test");
        
        try {
            HomePage homePage = new HomePage(WebDriverManager.getPage());
            test.log(Status.INFO, "Navigated to Yatra homepage");
            
            // Close login popup if present
            homePage.closeLoginPopupIfPresent();
            
            // Select Hotels tab
            homePage.selectTab("Hotels");
            test.log(Status.PASS, "Hotel tab selection test passed");
            
        } catch (PlaywrightException e) {
            test.log(Status.FAIL, "Playwright error: " + e.getMessage());
            Assert.fail("Playwright error: " + e.getMessage());
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Assertion failed: " + e.getMessage());
            throw e;
        }
    }

    @Test(priority = 3)
    public void testHolidayPackages() {
        ExtentTest test = ExtentManager.createTest("Holiday Packages Test");
        
        try {
            HomePage homePage = new HomePage(WebDriverManager.getPage());
            test.log(Status.INFO, "Navigated to Yatra homepage");
            
            // Close login popup if present
            homePage.closeLoginPopupIfPresent();
            
            // Select Holiday Packages tab
            homePage.selectTab("Holiday Packages");
            test.log(Status.PASS, "Holiday Packages tab selection test passed");
            
        } catch (PlaywrightException e) {
            test.log(Status.FAIL, "Playwright error: " + e.getMessage());
            Assert.fail("Playwright error: " + e.getMessage());
        } catch (AssertionError e) {
            test.log(Status.FAIL, "Assertion failed: " + e.getMessage());
            throw e;
        }
    }
}