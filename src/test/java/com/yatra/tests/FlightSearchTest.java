package com.yatra.tests;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.microsoft.playwright.PlaywrightException;
import com.playwright.core.WebDriverManager;
import com.playwright.utils.ExtentManager;
import com.playwright.utils.JsonDataReader;
import com.yatra.pages.FlightResultsPage;
import com.yatra.pages.FlightSearchPage;
import com.yatra.pages.HomePage;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

public class FlightSearchTest extends BaseTest {

    @DataProvider(name = "flightData")
    public Object[][] flightData() {
        return JsonDataReader.getTestData("flight-search.json", "flightSearchTests");
    }

    @Test(dataProvider = "flightData")
    public void testFlightSearch(Map<String, Object> data) {
        String testName = (String) data.get("testName");
        String departureCity = (String) data.get("departureCity");
        String arrivalCity = (String) data.get("arrivalCity");
        int adults = (int) data.get("adults");
        int children = (int) data.get("children");
        int infants = (int) data.get("infants");

        ExtentTest test = ExtentManager.createTest(testName);

        try {
            HomePage homePage = new HomePage(WebDriverManager.getPage());
            test.log(Status.INFO, "Navigated to Yatra homepage");

            homePage.closeLoginPopupIfPresent();

            homePage.selectTab("Flights");
            test.log(Status.INFO, "Selected Flights tab");

            FlightSearchPage flightSearchPage = homePage.getFlightSearchPage();

            flightSearchPage.enterDepartureCity(departureCity);
            test.log(Status.INFO, "Entered departure city: " + departureCity);

            flightSearchPage.enterArrivalCity(arrivalCity);
            test.log(Status.INFO, "Entered arrival city: " + arrivalCity);

            flightSearchPage.selectPassengers(adults, children, infants);
            test.log(Status.INFO, "Selected passengers - Adults: " + adults
                    + ", Children: " + children + ", Infants: " + infants);

            flightSearchPage.clickSearchFlights();
            test.log(Status.INFO, "Clicked search flights button");

            WebDriverManager.getPage().waitForTimeout(5000);

            FlightResultsPage resultsPage = new FlightResultsPage(WebDriverManager.getPage());

            Assert.assertTrue(resultsPage.areFlightResultsDisplayed(),
                "Flight results should be displayed");
            test.log(Status.PASS, testName + " completed successfully");

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

            homePage.closeLoginPopupIfPresent();

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

            homePage.closeLoginPopupIfPresent();

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
