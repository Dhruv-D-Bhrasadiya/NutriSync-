# Task 9 — Selenium Test Design and Local Execution

## Strategy
Use Selenium WebDriver 4 with JUnit 5 against a local application and isolated test database. Use synthetic data, explicit waits and stable element IDs/accessibility labels instead of layout-sensitive selectors. On failure capture screenshot, URL, test name and concise diagnostics without credentials.

## Critical journeys
| ID | Journey | Steps/setup | Expected assertions |
|---|---|---|---|
| E2E-01 | Register/sign in/out | Create unique demo client, logout, sign in | Account created; authenticated page appears; logout works |
| E2E-02 | Browse availability | Seed past/future/open/taken slots | Only future open slots visible and sorted |
| E2E-03 | Request booking | Choose open slot and submit | Booking ID and PENDING shown; slot no longer open |
| E2E-04 | Staff decision | Staff confirms pending request; client returns | CONFIRMED persists after refresh |
| E2E-05 | Cancel booking | Owner cancels eligible future request | CANCELLED shown; slot release follows policy |

Add negative tests for duplicate email, stale/taken slot, unauthorized decision, cross-user booking access and cancellation inside the restricted window. Service/security tests should cover cases awkward to automate reliably in the browser.

## Test data and structure
Use unique generated synthetic addresses at example.test. Seed a nutritionist, client and future slot sufficiently far ahead. Reset data or create isolated fixtures per test.

~~~text
src/test/java/.../
  pages/
  journeys/RegistrationJourneyTest.java
  journeys/AvailabilityJourneyTest.java
  journeys/BookingJourneyTest.java
  journeys/BookingDecisionJourneyTest.java
  journeys/CancellationJourneyTest.java
  support/TestDataFactory.java
  support/ScreenshotExtension.java
~~~

Add Selenium/JUnit dependencies and configure the base URL through an environment variable/property. Prefer Selenium Manager or a documented compatible driver setup. Headless Linux agents need a browser and OS libraries installed.

## Local execution
~~~bash
# Start the app and isolated test database in another terminal.
./mvnw -B test
./mvnw -B clean verify
~~~

A test profile or Maven Failsafe goal may be needed. Common Surefire reports are under target/surefire-reports; confirm actual report configuration.

## Acceptance
- Three to five high-value journeys implemented.
- Assertions test behavior/state, not just page title.
- Fixtures are isolated and repeatable.
- A controlled failure proves screenshot capture works.
- Real local report and command output saved.
- Tests pass after the controlled failure is restored.

See Task 10 for Jenkins integration.
