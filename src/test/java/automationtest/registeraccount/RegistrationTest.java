package automationtest.registeraccount;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationTest {
    private WebDriver driver;

    @BeforeEach
    public void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
    }

    @ParameterizedTest
    @MethodSource("testData")
    public void testRegistration(String fullName, String email, String phoneNumber, String password,
                                 String confirmPassword, boolean isCustomer, boolean terms, String expectedAlertText) {
        driver.get("http://localhost:3000/login-register");

        WebElement modalOpener = driver.findElement(By.id("open-button"));
        modalOpener.click();
        sleep(500);

        driver.findElement(By.id("register-fullName")).sendKeys(fullName);
        driver.findElement(By.id("register-email")).sendKeys(email);
        driver.findElement(By.id("register-phoneNumber")).sendKeys(phoneNumber);
        driver.findElement(By.id("register-password")).sendKeys(password);
        driver.findElement(By.id("register-confirmPassword")).sendKeys(confirmPassword);

        if (isCustomer) {
            driver.findElement(By.id("register-isCustomer-true")).click();
        } else {
            driver.findElement(By.id("register-isCustomer-false")).click();
        }

        WebElement termsField = driver.findElement(By.id("register-terms"));
        if (terms && !termsField.isSelected()) {
            termsField.click();
        }

        driver.findElement(By.id("register-submit")).click();

        // Handle Alert
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));
        wait.until(ExpectedConditions.alertIsPresent());
        Alert alert = driver.switchTo().alert();

        String alertText = alert.getText().trim();
        alert.accept();  // Accept the alert (click OK)

        // Assert alert text
        assertEquals(expectedAlertText, alertText);
    }

    static Stream<Arguments> testData() {
        return Stream.of(
                Arguments.of("", "", "", "", "",null, null, "Phone number is required."),
                Arguments.of("user", "user123@example.com", "0234563392", "Test@1234", "Test@1234", true, true, "Create account successfully. Please check your email inbox to verify your email address."),
                Arguments.of("user1", "user2@example.com", "0972427628", "Test@1234", "Test@1234", true, true, "The full name can only contain alphabet characters."),
                Arguments.of("User", "user1@example.com", "0972427777", "Test@1234", "Test@1234", true, true, "Email already existed. Please try another email."),
                Arguments.of("user", "userexample.com", "0342567892", "Test@1234", "Test@1234", true, true, "Please enter a valid email address"),
                Arguments.of("user", "user@example.com", "0248884567", "12345", "12345", true, true, "Password must contain at least one number, one numeral, and seven characters."),
                Arguments.of("user", "user@example.com", "0234567892", "Test@1234", "Test@1234", true, true, "The phone number already existed. Please try another phone number"),
                Arguments.of("user1", "user@example.com", "0123456789", "12345", "12345678", true, true, "Password not match"),
                //Arguments.of("user1", "user@example.com", "0123456789", "Test@1234", "Test@1234", false, false, "You must accept terms and conditions"),
                Arguments.of("user", "user@example.com", "2345678910", "Test@1234", "Test@1234", false, true, "Invalid phone number.")
        );
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
