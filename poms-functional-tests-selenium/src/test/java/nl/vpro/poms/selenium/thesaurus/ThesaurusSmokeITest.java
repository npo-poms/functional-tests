package nl.vpro.poms.selenium.thesaurus;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

/**
 * The type Thesaurus smoke itest.
 */
@FixMethodOrder(NAME_ASCENDING)
@Slf4j
@Ignore("Credentials not yet arranged")
public class ThesaurusSmokeITest {

    private static WebDriver driver;

    /**
     * Sets up. the
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(false);

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        driver.get("https://demo-cgms-gtaa.binnenkort-op.vpro.nl/");

        driver.findElement(By.id("username")).sendKeys("<user here>");
        driver.findElement(By.id("password")).sendKeys("<password here>");
        driver.findElement(By.id("kc-login")).click();
    }


    /**
     * Logs in to the thesaurus frontend.
     * <p>
     * TODO: Ideally this should be implemented with an ExpectedCondition
     *
     * @throws Exception throws all exceptions
     */
    @Test
    public void test001Login() throws Exception {
        assertEquals("POMS GTAA", driver.getTitle());
    }

    /**
     * Searches for a person with the Given name: Jan Peter and Family name Balkenende.
     * A list should appear with two values containing "Jan Peter"
     * <p>
     * TODO: Ideally this should be implemented with an ExpectedCondition
     *
     * @throws Exception throws all exceptions
     */
    @Test
    public void test002FindJanPeter() throws Exception {
        driver.findElement(By.id("givenName")).sendKeys("Jan Peter");
        driver.findElement(By.id("familyName")).sendKeys("Balkenende");
        driver.findElement(By.id("open")).click();

        for (String windowHandleId : driver.getWindowHandles()) {

            driver.switchTo().window(windowHandleId);
            if (driver.getTitle().equals("GTAA")) {
                break;
            }

        }

        long counter = driver.findElements(By.xpath("//ul/li"))
                .stream()
                .filter(s -> s.getText().contains("Jan Peter"))
                .count();

        assertEquals(2, counter);
    }


    /**
     * Tear down.
     *
     * @throws Exception the exception
     */
    @AfterClass
    public static void tearDown() throws Exception {
        driver.quit();
    }
}
