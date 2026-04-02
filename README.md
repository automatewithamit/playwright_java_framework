# Playwright Java Framework for Yatra.com Testing

A scalable, production-ready test automation framework built with Playwright, Java 21, and TestNG for testing [Yatra.com](https://www.yatra.com).

---

## Framework Structure

```
playwright_java_framework/
├── src/main/java/                          # Framework + Page Objects
│   ├── com/playwright/
│   │   ├── core/
│   │   │   ├── BasePage.java               # Abstract base for all page objects
│   │   │   ├── Navigation.java             # URL and timeout constants
│   │   │   └── WebDriverManager.java       # Thread-safe browser lifecycle
│   │   ├── listeners/
│   │   │   ├── TestListener.java           # Auto reporting + screenshot on failure
│   │   │   ├── RetryAnalyzer.java          # Auto-retry failed tests
│   │   │   └── RetryTransformer.java       # Applies retry to all @Test methods
│   │   └── utils/
│   │       ├── ConfigReader.java           # Environment-aware config loader
│   │       ├── ExtentManager.java          # Thread-safe ExtentReports manager
│   │       └── JsonDataReader.java         # JSON test data reader
│   └── com/yatra/pages/
│       ├── HomePage.java                   # Yatra home page actions
│       ├── FlightSearchPage.java           # Flight search form interactions
│       ├── FlightResultsPage.java          # Search results page
│       └── LoginPopupPage.java             # Login popup handler
│
├── src/test/java/                          # Tests only
│   └── com/yatra/tests/
│       ├── BaseTest.java                   # Test lifecycle (setup/teardown)
│       ├── FlightSearchTest.java           # Data-driven flight search tests
│       └── FrameworkValidationTest.java    # Framework smoke test
│
├── src/test/resources/
│   ├── testdata/
│   │   ├── flight-search.json              # Flight search test data
│   │   ├── hotel-search.json               # Hotel search test data
│   │   └── users.json                      # User test data
│   ├── config.properties                   # Default configuration
│   ├── config-dev.properties               # Dev environment config
│   ├── config-staging.properties           # Staging environment config
│   ├── config-prod.properties              # Production environment config
│   └── testng.xml                          # TestNG suite configuration
│
└── pom.xml                                 # Maven dependencies
```

---

## Architecture & Design Decisions

### Why This Structure?

The framework follows a strict **separation of concerns**:

- `src/main/java` contains **all reusable code** — framework core, utilities, page objects. This code never changes when you add new tests.
- `src/test/java` contains **only test classes**. When the test suite grows, you only add files here.
- `src/test/resources` contains **all external data** — configs, test data, suite definitions. Changes here require zero code changes.

This means a new team member can write tests by only touching `src/test/java` and `src/test/resources`.

---

## Feature Deep Dive

### 1. BrowserContext-Based Parallel Execution

**Problem:** Running tests in parallel with separate browser processes is slow and memory-heavy.

**Solution:** We use Playwright's BrowserContext isolation — one shared browser process, with each test getting its own isolated context.

```
@BeforeSuite  → Playwright.create() + browser.launch()     (once)
@BeforeMethod → browser.newContext() + context.newPage()    (per test, lightweight)
@AfterMethod  → context.close()                            (per test, fast)
@AfterSuite  → browser.close() + playwright.close()        (once)
```

**Why BrowserContext?**
- Each context has **completely isolated** cookies, localStorage, and sessions
- Creating a context is **~100x faster** than launching a new browser
- 3 parallel tests = 1 browser process instead of 3
- `ThreadLocal` ensures no state leaks between threads

**Key file:** `WebDriverManager.java`
- `launchBrowser()` — `synchronized`, called once, shared `Playwright` + `Browser`
- `createContext()` — per-thread via `ThreadLocal<BrowserContext>` and `ThreadLocal<Page>`
- `closeContext()` — cleans up thread-local resources + calls `ThreadLocal.remove()` to prevent memory leaks
- `quitBrowser()` — `synchronized`, shuts down shared instances

**Configured in:** `testng.xml`
```xml
<suite parallel="methods" thread-count="3">
```

---

### 2. Custom TestNG Listener (Auto Reporting + Screenshot on Failure)

**Problem:** Every test method had repetitive boilerplate — `ExtentManager.createTest()`, try/catch blocks, `Status.PASS`/`Status.FAIL` logging, manual screenshot capture.

**Solution:** A single `TestListener` that hooks into TestNG's lifecycle and handles all of this automatically.

**What it does:**

| TestNG Event | Listener Action |
|---|---|
| `onTestStart` | Creates ExtentTest with smart name (includes DataProvider params) |
| `onTestSuccess` | Logs PASS in ExtentReport |
| `onTestFailure` | Logs FAIL + captures full-page screenshot + embeds as base64 in report + saves PNG to disk |
| `onTestSkipped` | Logs SKIP with reason |

**Smart test naming for DataProvider tests:**
- Regular test: `testHotelSearch`
- DataProvider test: `testFlightSearch [Delhi to Mumbai Flight Search]`

The listener reads the `testName` key from the DataProvider's `Map<String, Object>` parameter automatically.

**Before the listener (every test had this):**
```java
@Test
public void testHotelSearch() {
    ExtentTest test = ExtentManager.createTest("Hotel Search Test");
    try {
        // test logic
        test.log(Status.PASS, "passed");
    } catch (PlaywrightException e) {
        test.log(Status.FAIL, e.getMessage());
        Assert.fail(e.getMessage());
    } catch (AssertionError e) {
        test.log(Status.FAIL, e.getMessage());
        throw e;
    }
}
```

**After the listener (clean test):**
```java
@Test
public void testHotelSearch() {
    HomePage homePage = new HomePage(WebDriverManager.getPage());
    homePage.closeLoginPopupIfPresent();
    homePage.selectTab("Hotels");
}
```

**Registered via:** `@Listeners(TestListener.class)` on `BaseTest`

---

### 3. Retry Mechanism

**Problem:** Flaky tests caused by network latency, dynamic page loading, or CI environment instability lead to false failures.

**Solution:** Two classes working together:

- `RetryAnalyzer` — implements `IRetryAnalyzer`, retries a failed test up to `MAX_RETRY` (default: 1) times before marking it as failed.
- `RetryTransformer` — implements `IAnnotationTransformer`, automatically applies `RetryAnalyzer` to every `@Test` method at runtime. No need to annotate each test individually.

**Registered in:** `testng.xml`
```xml
<listeners>
    <listener class-name="com.playwright.listeners.RetryTransformer"/>
</listeners>
```

**Why two classes?** Without `RetryTransformer`, you'd need `@Test(retryAnalyzer = RetryAnalyzer.class)` on every single test method. The transformer eliminates that.

---

### 4. Environment / Profile Support

**Problem:** Same tests need to run against dev, staging, and production with different URLs, timeouts, and browser settings.

**Solution:** `ConfigReader` loads environment-specific config files based on a `-Denv` system property.

**Resolution order (highest priority wins):**
1. **System property** — `mvn test -Dbrowser=firefox` overrides everything
2. **Environment config** — `config-{env}.properties` based on `-Denv=staging`
3. **Default config** — `config.properties` as fallback

**Usage:**
```bash
mvn test                                    # loads config-dev.properties
mvn test -Denv=staging                      # loads config-staging.properties
mvn test -Denv=prod                         # loads config-prod.properties
mvn test -Denv=staging -Dbrowser=firefox    # staging config + firefox override
```

**Why this approach?**
- Zero code changes to switch environments
- CI/CD pipelines just pass `-Denv=prod`
- Individual properties can be overridden without creating a new config file
- ExtentReport shows which environment, browser, and URL were used

---

### 5. JSON Test Data Externalization

**Problem:** Hardcoded test data in Java constants (`TestData.java`) means any data change requires recompilation. Adding new test scenarios requires code changes.

**Solution:** Test data lives in JSON files under `src/test/resources/testdata/`. The `JsonDataReader` utility reads them and returns data in TestNG `DataProvider` format.

**Example JSON (`flight-search.json`):**
```json
{
  "flightSearchTests": [
    {
      "testName": "Delhi to Mumbai Flight Search",
      "departureCity": "Delhi",
      "arrivalCity": "Mumbai",
      "adults": 2,
      "children": 0,
      "infants": 0
    }
  ]
}
```

**Usage in test:**
```java
@DataProvider(name = "flightData")
public Object[][] flightData() {
    return JsonDataReader.getTestData("flight-search.json", "flightSearchTests");
}

@Test(dataProvider = "flightData")
public void testFlightSearch(Map<String, Object> data) {
    String city = (String) data.get("departureCity");
    int adults = (int) data.get("adults");
}
```

**Why JSON?**
- Add new test scenarios by editing a JSON file — no recompilation
- Non-developers (QA, BA) can add test data
- Supports all types: String, int, boolean, double
- `getTestDataByIndex()` available for single-record access
- Jackson ObjectMapper handles parsing (already a dependency)

---

### 6. Page Object Model

**Problem:** Mixing page element locators with test logic makes tests brittle and hard to maintain.

**Solution:** Each page has its own class extending `BasePage`. Page classes live in `src/main/java` (framework code), tests live in `src/test/java`.

**BasePage** provides common operations:
- `click()`, `fill()`, `getText()`, `isVisible()`, `selectOption()`
- `waitForSelector()`, `waitForPageLoad()`

**Page classes** encapsulate page-specific logic:
- `HomePage` — tab navigation, login popup handling
- `FlightSearchPage` — city selection, passenger count, search
- `FlightResultsPage` — results verification, filtering, sorting
- `LoginPopupPage` — popup detection and dismissal

**Input validation** is built into page methods:
```java
public void enterDepartureCity(String departureCity) {
    if (departureCity == null || departureCity.trim().isEmpty()) {
        throw new IllegalArgumentException("Departure city cannot be null or empty");
    }
    // ...
}
```

---

### 7. ExtentReports Integration

**Problem:** Need rich HTML reports showing test results, environment info, and failure screenshots.

**Solution:** `ExtentManager` provides a thread-safe wrapper around ExtentReports.

**Thread safety:**
- `ExtentReports` instance is shared (it's internally thread-safe for writing)
- `ExtentTest` is per-thread via `ThreadLocal`
- `initializeExtentReports()` and `createTest()` are `synchronized`

**Report includes:**
- OS, Java version, user
- Environment name, browser, base URL
- Per-test PASS/FAIL/SKIP status
- Embedded screenshots on failure (base64)
- Test step logs via `ExtentManager.getTest().log(Status.INFO, "message")`

**Output:** `test-output/ExtentReport.html`

---

## Setup Instructions

### Prerequisites
- Java 21
- Maven 3.6+
- IDE (IntelliJ IDEA / Eclipse)

### Install Dependencies
```bash
mvn clean install
```

### Install Playwright Browsers
```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

---

## Running Tests

```bash
# Default (dev environment, Chrome)
mvn test

# Specific environment
mvn test -Denv=staging
mvn test -Denv=prod

# Specific browser
mvn test -Dbrowser=firefox
mvn test -Dbrowser=webkit

# Combined
mvn test -Denv=staging -Dbrowser=firefox -Dheadless=true

# Specific test class
mvn test -Dtest=FlightSearchTest

# From IDE
# Right-click test class → Run
# Or run testng.xml directly
```

---

## Test Reports

After execution:
- **ExtentReports:** `test-output/ExtentReport.html` — rich HTML with screenshots
- **Screenshots:** `test-output/screenshots/` — PNG files for failed tests
- **TestNG Reports:** `test-output/index.html` — standard TestNG output

---

## Adding New Tests

1. **Add test data** — Create/edit JSON file in `src/test/resources/testdata/`
2. **Add page object** (if new page) — Create class in `com.yatra.pages` extending `BasePage`
3. **Add test class** — Create class in `com.yatra.tests` extending `BaseTest`
4. **Write test method** — Use `@Test` + `@DataProvider` if data-driven
5. **Register in testng.xml** — Add class to suite

**Example minimal test:**
```java
public class NewFeatureTest extends BaseTest {

    @Test
    public void testNewFeature() {
        HomePage homePage = new HomePage(WebDriverManager.getPage());
        homePage.closeLoginPopupIfPresent();
        // your test logic — listener handles reporting automatically
    }
}
```

No try/catch needed. No ExtentManager.createTest() needed. The listener handles everything.

---

## Configuration

### config.properties
```properties
browser=chrome          # chrome, firefox, webkit
url=https://www.yatra.com
timeout=30
headless=false          # true for CI/CD
```

### Environment Overrides
| File | Purpose |
|---|---|
| `config-dev.properties` | Local development (headless=false) |
| `config-staging.properties` | Staging environment (headless=true, staging URL) |
| `config-prod.properties` | Production (headless=true, longer timeout) |

---

## Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| Playwright | 1.57.0 | Browser automation |
| TestNG | 7.8.0 | Test framework + parallel execution |
| ExtentReports | 5.1.1 | HTML test reporting |
| Jackson Databind | 2.15.2 | JSON test data parsing |

---

## Test Lifecycle Flow

```
@BeforeSuite
  ├── ExtentManager.initializeExtentReports()
  └── WebDriverManager.launchBrowser("chrome")     ← one browser for all tests

@BeforeMethod (per test)
  ├── WebDriverManager.createContext()              ← isolated context per test
  └── WebDriverManager.navigateTo(url)

TestListener.onTestStart()                          ← auto-creates ExtentTest

  @Test method executes                             ← your test logic

TestListener.onTestSuccess/Failure/Skipped()        ← auto-logs result + screenshot

  If failed → RetryAnalyzer.retry()                 ← auto-retries once

@AfterMethod (per test)
  └── WebDriverManager.closeContext()               ← cleans up context

@AfterSuite
  ├── WebDriverManager.quitBrowser()                ← shuts down browser
  └── ExtentManager.flushReports()                  ← writes HTML report
```
