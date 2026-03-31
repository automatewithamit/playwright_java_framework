# Playwright Java Framework for Yatra.com Testing

## Framework Structure

```
playwright_java_tutorial/
├── src/
│   ├── main/java/
│   │   ├── com/playwright/core/          # Core framework classes
│   │   │   ├── BasePage.java             # Base page with common methods
│   │   │   ├── Navigation.java           # Navigation constants
│   │   │   └── WebDriverManager.java     # Browser management
│   │   ├── com/playwright/utils/         # Utility classes
│   │   │   ├── ConfigReader.java         # Configuration reader
│   │   │   ├── ExtentManager.java        # Reporting utility
│   │   │   └── TestData.java             # Test data constants
│   │   └── com/yatra/pages/              # Page Object Model classes
│   │       ├── HomePage.java             # Yatra home page
│   │       ├── FlightSearchPage.java     # Flight search functionality
│   │       ├── FlightResultsPage.java    # Flight results page
│   │       └── LoginPopupPage.java       # Login popup handling
│   └── test/java/
│       └── com/yatra/tests/              # Test classes
│           ├── BaseTest.java             # Base test setup/teardown
│           ├── FlightSearchTest.java     # Flight search tests
│           └── FrameworkValidationTest.java # Framework validation
├── src/test/resources/
│   ├── config.properties                 # Configuration properties
│   └── testng.xml                        # TestNG configuration
└── pom.xml                               # Maven dependencies

```

## Framework Architecture

### **Core Framework (src/main/java/com/playwright/)**
- **core/**: Base classes for pages and tests
- **utils/**: Utility classes for configuration, reporting

### **Application Specific (src/main/java/com/yatra/)**
- **pages/**: Page Object Model classes for Yatra.com

### **Tests Only (src/test/java/com/yatra/)**
- **tests/**: Actual test classes that extend BaseTest

## Features

- **Clean Architecture**: Framework code separated from test code
- **Page Object Model (POM)**: Clean separation of page elements and test logic
- **Base Classes**: Reusable base page and test classes in framework
- **Configuration Management**: External configuration via properties file
- **ExtentReports Integration**: Comprehensive test reporting with screenshots
- **Cross-browser Support**: Chrome, Firefox, Safari support
- **TestNG Integration**: Organized test execution with TestNG
- **Security**: Updated dependencies and secure coding practices
- **Simple Logging**: Console output + ExtentReports (no complex logging framework)

## Setup Instructions

1. **Prerequisites**:
   - Java 21
   - Maven 3.6+
   - IDE (IntelliJ IDEA/Eclipse)

2. **Install Dependencies**:
   ```bash
   mvn clean install
   ```

3. **Install Playwright Browsers**:
   ```bash
   mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
   ```

## Running Tests

### Command Line
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=FlightSearchTest

# Run with specific browser
mvn test -Dbrowser=firefox
```

### IDE
- Right-click on test class and select "Run"
- Use TestNG XML file for suite execution

## Configuration

Edit `src/test/resources/config.properties`:
```properties
browser=chrome          # chrome, firefox, safari
url=https://www.yatra.com
timeout=30
headless=false         # true for headless execution
```

## Test Reports

After test execution, find reports at:
- **ExtentReports**: `test-output/ExtentReport.html`
- **TestNG Reports**: `test-output/index.html`
- **Console Output**: Real-time test execution logs

## Adding New Tests

1. Create page class in `com.yatra.pages` (src/main/java)
2. Extend `BasePage` for common functionality
3. Create test class in `com.yatra.tests` (src/test/java)
4. Extend `BaseTest` for setup/teardown
5. Add test methods with `@Test` annotation
6. Use ExtentReports for detailed logging: `test.log(Status.INFO, "message")`

## Framework Design Principles

- **Separation of Concerns**: Framework code vs Test code
- **Reusability**: Common functionality in base classes
- **Maintainability**: Clean structure and simple logging
- **Scalability**: Easy to add new pages and tests
- **Security**: Updated dependencies and secure practices
- **Simplicity**: ExtentReports + Console output (no complex logging)

## Best Practices

- Use Page Object Model pattern
- Keep test data in `TestData` class
- Use meaningful test names and descriptions
- Add proper assertions
- Use ExtentReports for test step logging
- Use System.out.println for console debugging
- Handle dynamic elements with proper waits
- Keep framework code separate from test code

## Logging Strategy

### **Simple & Effective Approach:**
- **ExtentReports**: Comprehensive test reporting with screenshots
- **Console Output**: Real-time debugging with System.out.println
- **TestNG Reports**: Built-in TestNG reporting

### **Why This Approach:**
- ✅ **No dependency conflicts**: No Log4j2 or SLF4J issues
- ✅ **Simple maintenance**: Easy to understand and maintain
- ✅ **Comprehensive reporting**: ExtentReports provides rich HTML reports
- ✅ **Real-time feedback**: Console output for immediate debugging
- ✅ **Built-in integration**: Works seamlessly with TestNG and CI/CD

### **Usage Examples:**
```java
// In test classes - use ExtentReports
ExtentTest test = ExtentManager.createTest("Test Name");
test.log(Status.INFO, "Test step completed");
test.log(Status.PASS, "Assertion passed");

// For debugging - use console output
System.out.println("Debug info: " + variable);
```