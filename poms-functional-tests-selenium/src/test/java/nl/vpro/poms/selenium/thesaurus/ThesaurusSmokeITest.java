package nl.vpro.poms.selenium.thesaurus;

import lombok.extern.slf4j.Slf4j;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.openqa.selenium.By;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.config.Webtest;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

/**
 * The type Thesaurus smoke itest.
 */
@FixMethodOrder(NAME_ASCENDING)
@Slf4j
public class ThesaurusSmokeITest extends Webtest {

    /**
     * Sets up. the
     *
     */
    @BeforeClass
    public static void setUp() {
        loginGtaaBrowserTest();

        // after logging in first we have to go to the demo interface again, because of
        //sso choices.
        String url = CONFIG.getProperties(Config.Prefix.npo_api).get("baseUrl") + "/thesaurus/example/";
        log.info("Using {}", url);
        driver.get(url);

    }


    /**
     * Logs in to the thesaurus frontend.
     * <p>
     * TODO: Ideally this should be implemented with an ExpectedCondition
     *
     */
    @Test
    public void test001Login() {
        assertThat(driver.getTitle()).isEqualTo("POMS GTAA");
    }

    /**
     * Searches for a person with the Given name: Jan Peter and Family name Balkenende.
     * A list should appear with two values containing "Jan Peter"
     * <p>
     * TODO: Ideally this should be implemented with an ExpectedCondition
     *
     */
    @Test
    public void test002FindJanPeter() {
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

        assertThat(counter).isGreaterThanOrEqualTo(2);
    }

}
