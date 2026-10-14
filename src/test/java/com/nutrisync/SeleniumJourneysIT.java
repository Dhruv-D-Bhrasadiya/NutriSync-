package com.nutrisync;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "e2e.enabled", matches = "true")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SeleniumJourneysIT {
    private WebDriver driver;
    private WebDriverWait wait;

    @RegisterExtension
    final TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        protected void testFailed(ExtensionContext context, Throwable cause) {
            if (driver instanceof TakesScreenshot screenshotDriver) {
                try {
                    Path directory = Path.of("target", "selenium-failures");
                    Files.createDirectories(directory);
                    String safeName = context.getRequiredTestMethod().getName().replaceAll("[^A-Za-z0-9._-]", "_");
                    Files.write(directory.resolve(safeName + ".png"), screenshotDriver.getScreenshotAs(OutputType.BYTES));
                } catch (IOException | WebDriverException ignored) {
                    // Keep the original test failure; screenshot capture is best-effort.
                }
            }
        }
    };

    @BeforeEach
    void createBrowser() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage", "--window-size=1440,1000");
        String remoteUrl = System.getProperty("e2e.remoteUrl", "").trim();
        driver = remoteUrl.isBlank() ? new ChromeDriver(options) : new RemoteWebDriver(URI.create(remoteUrl).toURL(), options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    void closeBrowser() {
        if (driver != null) driver.quit();
    }

    @Test @Order(1)
    void registrationCreatesAccountAndShowsSuccess() {
        String email = "e2e-" + UUID.randomUUID() + "@example.test";
        driver.get(baseUrl() + "/register");
        driver.findElement(By.id("name")).sendKeys("Selenium Client");
        driver.findElement(By.id("email")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys("SeleniumPass!2026");
        driver.findElement(By.id("register-submit")).click();
        wait.until(ExpectedConditions.urlContains("/login"));
        assertThat(driver.getPageSource()).contains("Registration successful");
    }

    @Test @Order(2)
    void futureAvailabilityIsVisible() {
        driver.get(baseUrl() + "/slots");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".slot-card")));
        assertThat(driver.findElements(By.cssSelector(".slot-card"))).isNotEmpty();
        assertThat(driver.getPageSource()).contains("Available consultations");
    }

    @Test @Order(3)
    void clientCanRequestBookingAndSeePendingStatus() {
        login("client@nutrisync.local", "NutriSyncDemo!2026");
        requestFirstAvailableSlot();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[id^='booking-row-']")));
        assertThat(driver.getPageSource()).contains("PENDING");
    }

    @Test @Order(4)
    void nutritionistCanConfirmTheBooking() {
        login("client@nutrisync.local", "NutriSyncDemo!2026");
        requestFirstAvailableSlot();
        WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[id^='booking-row-']")));
        String bookingId = row.getAttribute("data-booking-id");
        driver.findElement(By.id("logout-button")).click();
        login("nutritionist@nutrisync.local", "NutriSyncDemo!2026");
        driver.get(baseUrl() + "/staff/bookings");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("confirm-booking-" + bookingId)));
        driver.findElement(By.id("confirm-booking-" + bookingId)).click();
        wait.until(ExpectedConditions.urlContains("/staff/bookings"));
        assertThat(driver.getPageSource()).contains("Booking confirmed.");
    }

    @Test @Order(5)
    void clientCanCancelAReservationWithEnoughNotice() {
        login("client@nutrisync.local", "NutriSyncDemo!2026");
        requestFirstAvailableSlot();
        WebElement row = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("[id^='booking-row-']")));
        String bookingId = row.getAttribute("data-booking-id");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("cancel-booking-" + bookingId)));
        driver.findElement(By.id("cancel-booking-" + bookingId)).click();
        wait.until(ExpectedConditions.urlContains("/bookings"));
        assertThat(driver.findElement(By.id("booking-row-" + bookingId)).getText()).contains("CANCELLED");
    }

    private void login(String email, String password) {
        driver.get(baseUrl() + "/login");
        driver.findElement(By.id("username")).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("login-submit")).click();
        wait.until(ExpectedConditions.urlContains("/slots"));
    }

    private void requestFirstAvailableSlot() {
        driver.get(baseUrl() + "/slots");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[id^='book-slot-']")));
        button.click();
        wait.until(ExpectedConditions.urlContains("/bookings"));
    }

    private String baseUrl() {
        return System.getProperty("e2e.baseUrl", "http://localhost:8080").replaceAll("/+$", "");
    }
}
