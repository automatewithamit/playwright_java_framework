package com.yatra.tests;

import com.playwright.core.WebDriverManager;
import com.playwright.utils.ExtentManager;
import com.playwright.utils.JsonDataReader;
import com.aventstack.extentreports.Status;
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
        String departureCity = (String) data.get("departureCity");
        String arrivalCity = (String) data.get("arrivalCity");
        int adults = (int) data.get("adults");
        int children = (int) data.get("children");
        int infants = (int) data.get("infants");

        HomePage homePage = new HomePage(WebDriverManager.getPage());
        homePage.closeLoginPopupIfPresent();

        homePage.selectTab("Flights");
        ExtentManager.getTest().log(Status.INFO, "Selected Flights tab");

        FlightSearchPage flightSearchPage = homePage.getFlightSearchPage();

        flightSearchPage.enterDepartureCity(departureCity);
        ExtentManager.getTest().log(Status.INFO, "Departure: " + departureCity);

        flightSearchPage.enterArrivalCity(arrivalCity);
        ExtentManager.getTest().log(Status.INFO, "Arrival: " + arrivalCity);

        flightSearchPage.selectPassengers(adults, children, infants);
        ExtentManager.getTest().log(Status.INFO,
                "Passengers - Adults: " + adults + ", Children: " + children + ", Infants: " + infants);

        flightSearchPage.clickSearchFlights();
        WebDriverManager.getPage().waitForTimeout(5000);

        FlightResultsPage resultsPage = new FlightResultsPage(WebDriverManager.getPage());
        Assert.assertTrue(resultsPage.areFlightResultsDisplayed(),
                "Flight results should be displayed");
    }

    @Test
    public void testHotelSearch() {
        HomePage homePage = new HomePage(WebDriverManager.getPage());
        homePage.closeLoginPopupIfPresent();

        homePage.selectTab("Hotels");
        ExtentManager.getTest().log(Status.INFO, "Selected Hotels tab");
    }

    @Test
    public void testHolidayPackages() {
        HomePage homePage = new HomePage(WebDriverManager.getPage());
        homePage.closeLoginPopupIfPresent();

        homePage.selectTab("Holiday Packages");
        ExtentManager.getTest().log(Status.INFO, "Selected Holiday Packages tab");
    }
}
