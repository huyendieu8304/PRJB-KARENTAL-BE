//package com.mp.karental.automationtest.registeraccount;
//
//import io.github.bonigarcia.wdm.WebDriverManager;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.params.ParameterizedTest;
//import org.junit.jupiter.params.provider.MethodSource;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebDriver;
//import org.openqa.selenium.WebElement;
//import org.openqa.selenium.chrome.ChromeDriver;
//import org.openqa.selenium.chrome.ChromeOptions;
//import org.openqa.selenium.support.ui.ExpectedConditions;
//import org.openqa.selenium.support.ui.WebDriverWait;
//import org.springframework.test.annotation.DirtiesContext;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Stream;
//
//import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.fail;
//import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD;
//
//@DirtiesContext(classMode = AFTER_EACH_TEST_METHOD)
//// This class is to check if there is token sent in the welcome email
//public class VerifyEmailTest {
//    private static WebDriver driver;
//
//    @BeforeAll
//    public static void setUp() {
//        WebDriverManager.chromedriver().setup();
//        ChromeOptions options = new ChromeOptions();
////        options.addArguments("--headless"); // No UI
////        options.addArguments("--disable-gpu"); // For Windows
////        options.addArguments("--window-size=1920,1080"); // Ensures proper rendering
////        options.addArguments("--disable-extensions"); // Disables unnecessary extensions
////        options.addArguments("--disable-popup-blocking"); // Prevents popups from slowing down tests
////        options.addArguments("--blink-settings=imagesEnabled=false"); // Disables image loading for faster execution
////        options.addArguments("--disable-dev-shm-usage"); // Prevents crashes in Docker environments
////        options.addArguments("--no-sandbox"); // Bypass OS security restrictions
//        options.addArguments("user-data-dir=C:\\Users\\Anh Bui\\AppData\\Local\\Google\\Chrome\\User Data");
//        options.addArguments("--profile-directory=Default");
//        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36");
//
//        driver = new ChromeDriver();
//
//        driver.manage().window().maximize();
//    }
//    @ParameterizedTest
//    @MethodSource("testData")
//    public void verifyEmail(String emailUrl) {
//        // 1. Navigate to the email details page
//        driver.get("https://mailtrap.io"+emailUrl);
//        // 2. Wait for the right pane or message content to load
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        WebElement verifyEmailLink = wait.until(
//                ExpectedConditions.visibilityOfElementLocated(By.cssSelector("a.verify-email"))
//        );
//        String verifyUrl = verifyEmailLink.getAttribute("href");
//        assertTrue(verifyUrl.contains("/user/verify-email"));
//        assertTrue(verifyUrl.contains("t="));
//
//
//
//    }
//    // This method matches your code snippet, but returns a Stream for JUnit
//    static Stream<String> testData() {
//        return testDataList().stream();
//    }
//    public static List<String> testDataList() {
//        driver.get("https://mailtrap.io/inboxes/3515500/messages");
//        // Wait for the messages list to be visible
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        WebElement messagesList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div[data-test-id='messages_list']")));
//
//        // Locate all subject elements within the messages list
//        List<WebElement> emailSubjects = messagesList.findElements(By.cssSelector("span.subject"));
//        // Locate all <a> elements within the messages list
//        List<WebElement> emailLink = messagesList.findElements(By.cssSelector("a.i18m0o91"));
//
//        // Filter emails whose subject starts with "Welcome to Karental,"
//        List<String> welcomeEmailsLink = new ArrayList<>();
//        for (WebElement subjectElement : emailSubjects) {
//            String subjectText = subjectElement.getText().trim();
//            String href = emailLink.get(emailSubjects.indexOf(subjectElement)).getAttribute("href");
//            if (subjectText.startsWith("Welcome to Karental,")) {
//                welcomeEmailsLink.add(href);
//                System.out.println("Found welcome email: " + subjectText);
//            }
//        }
//
//        return  welcomeEmailsLink;
//
//    }
//    @AfterEach
//    public void tearDown() {
//        if (driver != null) {
//            driver.quit();
//        }
//    }
//}
