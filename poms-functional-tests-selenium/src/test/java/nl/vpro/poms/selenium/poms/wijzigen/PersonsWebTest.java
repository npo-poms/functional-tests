package nl.vpro.poms.selenium.poms.wijzigen;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import nl.vpro.api.client.utils.Config;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;

/**
 * @author Michiel Meeuwissen
 */

@Slf4j
@FixMethodOrder(NAME_ASCENDING)
public class PersonsWebTest extends AbstractTest {



    static String firstName = "Pietje";
    static String lastName = "Puk" + System.currentTimeMillis();

    public PersonsWebTest(@Nonnull WebDriverFactory.Browser browser, @Nonnull String version) {
        super(browser, version);
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
        waitForAngularRequestsToFinish();


        WebElement element = driver.findElement(By.cssSelector("#media-general-WO_VPRO_025057 > div.media-section-general-left > poms-persons > div > button"));
        Actions actions = new Actions(driver);
        actions.moveToElement(element);
        actions.perform();

        element.click();
        driver.findElement(By.cssSelector("#suggestions")).sendKeys(firstName + " " + lastName);
        waitForAngularRequestsToFinish();
        driver.findElement(By.cssSelector("div.col-12.personfields  span.new")).click();

        waitForAngularRequestsToFinish();

        driver.findElement(By.id("givenName")).sendKeys(firstName);
        driver.findElement(By.id("familyName")).sendKeys(lastName);
        driver.findElement(By.id("role")).sendKeys("Redacteur");
        waitForAngularRequestsToFinish();

        driver.findElement(By.cssSelector("#scroll-spy-top > div.modal.fade.modal-person.in > div > div > form > div.footer-container > div > button:nth-child(2)")).click();

        waitForAngularRequestsToFinish();


        // TODO add Checks.


    }
}
