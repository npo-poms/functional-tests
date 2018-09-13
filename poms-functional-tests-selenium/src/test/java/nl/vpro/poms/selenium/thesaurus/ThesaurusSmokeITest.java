package nl.vpro.poms.selenium.thesaurus;

import lombok.extern.slf4j.Slf4j;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.config.Webtest;
import org.junit.*;
import org.openqa.selenium.By;

import static org.junit.Assert.assertEquals;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

/**
 * The type Thesaurus smoke itest.
 */
@FixMethodOrder(NAME_ASCENDING)
@Slf4j
@Ignore("Credentials not yet arranged")
public class ThesaurusSmokeITest extends Webtest{

    /**
     * Sets up. the
     *
     * @throws Exception the exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        loginGtaaBrowserTest();

        // after logging in first we have to go to the demo interface again, because of
        //sso choices.
        String url = CONFIG.getProperties(Config.Prefix.npo_api).get("baseUrl") + "/thesaurus/example/person/";
        driver.get(url);

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
