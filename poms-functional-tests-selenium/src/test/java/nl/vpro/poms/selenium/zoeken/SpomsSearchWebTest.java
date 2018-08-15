package nl.vpro.poms.selenium.zoeken;

import lombok.extern.slf4j.Slf4j;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import com.paulhammant.ngwebdriver.NgWebDriver;

import nl.vpro.poms.config.Webtest;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;

/**
 * Selenium testcases to test Finding media objects.
 *
 *
 * @author e.kuijt@vpro.nl
 */
@FixMethodOrder(NAME_ASCENDING)
@Slf4j
public class SpomsSearchWebTest extends Webtest {

    /**
     * Sets up. a webdriver connected logged in to Poms interface using SpeciaalVfGebruiken from properties file
     */
    @BeforeClass
    public static void setUp() {
        loginVPROand3voor12();
    }

    /**
     * Use the search field to find 'klusjesmannen'
     */
    @Test
    public void search1() {
        NgWebDriver ngWebDriver = new NgWebDriver(driver);

        // Kies Nieuwe zoekopdracht
        ngWebDriver.waitForAngularRequestsToFinish();
        driver.findElement(By.cssSelector("a[title='Nieuwe zoekopdracht']")).click();

        // Selecteer zoekscherm, selecteer de zichtbare
        driver.findElements(By.cssSelector("input[id='query'][ng-model='formData.text']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .forEach(webElement -> webElement.sendKeys("De klusjesmannen"));

        // Selecteer 'Zoeken' knop selecteer de zichtbare
        driver.findElements(By.cssSelector("button[ng-click='searchFormController.submit()']"))
                .stream()
                .filter(WebElement::isDisplayed)
                .forEach(WebElement::click);

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
