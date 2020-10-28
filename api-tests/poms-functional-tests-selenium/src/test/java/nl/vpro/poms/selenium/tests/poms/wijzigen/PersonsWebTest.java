package nl.vpro.poms.selenium.tests.poms.wijzigen;

import lombok.extern.log4j.Log4j2;

import javax.annotation.Nonnull;

import org.junit.*;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.tests.poms.AbstractPomsTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

/**
 * @author Michiel Meeuwissen
 */

@Log4j2
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PersonsWebTest extends AbstractPomsTest {



    static String firstName = "Pietje";
    static String lastName = "Puk" + System.currentTimeMillis();

    public PersonsWebTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);

    }

    @Test
    public void test00Login() {
        login().VPROand3voor12();
    }

    /**
     * Create a clip, forgetting to fill in the genre.
     *
     * Nog in te vullen: Genre should be in the footer of the page
     */
    @Test
    public void test01OpenObject() {
        driver.get(CONFIG.getProperties(Config.Prefix.poms).get("baseUrl") + "/#/edit/" + MID);

    }
    @Test
    public void test02AddPerson() {
        webDriverUtil.waitForAngularRequestsToFinish();

        String selector = "button#addCredits";
        log.info("Opening persons of {}", selector);
        WebElement element = driver.findElement(By.cssSelector(selector));// FAILS

        webDriverUtil.scrollIntoView(element);

        log.info("Clicking {}", element);

        element.click();
        driver.findElement(By.cssSelector("#suggestions")).sendKeys(firstName + " " + lastName);
        webDriverUtil.waitForAngularRequestsToFinish();
        driver.findElement(By.cssSelector("div.col-12.personfields  span.new")).click();

        webDriverUtil.waitForAngularRequestsToFinish();

        log.info("Checking contents");

        driver.findElement(By.id("givenName")).sendKeys(firstName);
        driver.findElement(By.id("familyName")).sendKeys(lastName);
        driver.findElement(By.id("role")).sendKeys("Redacteur");
        webDriverUtil.waitForAngularRequestsToFinish();

        driver.findElement(By.cssSelector("#scroll-spy-top > div.modal.fade.modal-person.in > div > div > form > div.footer-container > div > button:nth-child(2)")).click();

        webDriverUtil.waitForAngularRequestsToFinish();


        // TODO add Checks.


    }
}
