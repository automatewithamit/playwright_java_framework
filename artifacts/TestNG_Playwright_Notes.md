# TestNG & Playwright Framework — Lecture Notes

---

## 1. Test Listeners

### What are Test Listeners?
Test Listeners **listen and respond to test events** — based on the outcome of a test, they trigger specific actions.

### Key Methods in ITestListener:
| Method | When it triggers |
|---|---|
| `onTestStart` | When a test starts |
| `onTestSuccess` | When a test passes ✅ |
| `onTestFailure` | When a test fails ❌ |
| `onTestSkipped` | When a test is skipped ⏭️ |
| `onStart` | When test suite starts |
| `onFinish` | When test suite finishes |

### How to Implement:
```java
public class MyTestListener implements ITestListener {

    @Override
    public void onTestStart(ITestResult result) {
        // log that test has started
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        // stop network trace WITHOUT saving
        // mark test as passed in report
        // log that test has passed
    }

    @Override
    public void onTestFailure(ITestResult result) {
        // stop network trace AND SAVE it
        // capture screenshot
        // mark test as failed in report
        // log that test has failed
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        // log that test was skipped
    }
}
```

### What are Test Listeners used for?
```
✅ Logging test results
✅ Capturing screenshots on failure
✅ Saving network traces on failure
✅ Generating HTML reports (Extent Reports)
✅ Running tests on specific environments
```

### Screenshot Capture Logic:
```
onTestSuccess → STOP trace, DON'T save
               (no point saving if test passed!)

onTestFailure → STOP trace, SAVE network logs
               (need logs to debug the failure!)
               + capture screenshot
```

### Screenshot method:
```java
// Takes screenshot and saves as image file
// Returns byte array → must convert to image file
// Only called on test failure — saves disk space!
```

---

## 2. Test Listeners vs Hooks

| | Test Listeners | Hooks |
|---|---|---|
| **Purpose** | Listen & respond to test outcomes | Setup & teardown |
| **Scope** | Narrow — test level only | Broad — suite, class, group, method |
| **When triggered** | Based on pass/fail outcome | Before/after test execution |
| **Use cases** | Screenshots, logging, reporting | Open browser, close browser, clear data |

### Execution Order:
```
Hooks (Before Method) → runs first
Test Listeners (onTestStart) → runs after hooks
```

### Key Difference:
```
Hooks     → PREPARE and CLEAN UP
            "get things ready before test"
            "clean up after test"

Listeners → REACT to outcomes
            "test passed → do this"
            "test failed → do that"
```

### Scope of Hooks (broader):
```
@BeforeSuite   → suite level
@BeforeTest    → test level
@BeforeClass   → class level
@BeforeGroups  → group level
@BeforeMethod  → method level
```

---

## 3. Retry Mechanism

### What is it?
Automatically retries failed tests — useful for:
```
→ Slow test environments
→ Flaky tests
→ Network issues
```

### Two interfaces needed:

#### Interface 1: IAnnotationTransformer
```java
public class RetryTransformer implements IAnnotationTransformer {
    @Override
    public void transform(...) {
        annotation.setAnalyzer(RetryAnalyzer.class);
    }
}
```

#### Interface 2: IRetryAnalyzer
```java
public class RetryAnalyzer implements IRetryAnalyzer {
    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            return true;  // retry!
        }
        return false;  // don't retry
    }
}
```

### Must add to testng.xml:
```xml
<listeners>
    <listener class-name="path.to.RetryTransformer"/>
</listeners>
```

### Summary:
```
RetryTransformer → implements IAnnotationTransformer
                 → has transform() method
                 → links to RetryAnalyzer class

RetryAnalyzer    → implements IRetryAnalyzer
                 → has retry() method
                 → returns true (retry) or false (stop)
```

---

## 4. Data Driven Testing

### What is it?
Running the same test with **multiple sets of data** automatically.

### How to implement:

#### Step 1 — Add @DataProvider annotation:
```java
@DataProvider(name = "slideData")
public Object[][] getSlideData() {
    // reads from JSON/Excel/CSV/SQL
    return new Object[][] {
        {"departure", "arrival", 1, 0, 0},
        {"departure2", "arrival2", 2, 1, 0}
    };
}
```

#### Step 2 — Use in test:
```java
@Test(dataProvider = "slideData")
public void flightSearchTest(String departure, String arrival, 
                              int adults, int children, int infants) {
    // test runs once for each row of data!
}
```

### Data Sources supported:
```
✅ JSON files
✅ Excel files
✅ CSV files
✅ SQL databases
✅ Any other source
```

### Important Rules:
```
1. Return type must be Object[][]
2. @DataProvider name must EXACTLY match 
   dataProvider name in @Test
3. If DataProvider is in different class:
   @Test(dataProvider = "name", 
         dataProviderClass = MyDataClass.class)
```

---

## 5. Coming Up Next

```
→ Extent Manager (HTML Reporting)
→ Logging implementation
→ Network tracing in Playwright
→ API Testing in Playwright
→ pom.xml configuration
→ testng.xml — test execution strategy
→ Test execution strategies
```

---

## Quick Interview Reference

```
Q: What interface implements Test Listeners?
A: ITestListener

Q: What interface implements Retry?
A: IAnnotationTransformer + IRetryAnalyzer

Q: Difference between Listeners and Hooks?
A: Listeners react to outcomes (pass/fail)
   Hooks setup/teardown before/after tests

Q: What is Data Provider return type?
A: Object[][] (double dimension array)

Q: When do you capture screenshots?
A: Only on test failure — saves disk space!
```
