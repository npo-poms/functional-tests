package nl.vpro.poms.selenium.poms.zoeken;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nonnull;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

import static org.junit.runners.MethodSorters.NAME_ASCENDING;

/**
 * Selenium testcases to test Finding media objects.
 *
 *
 * @author e.kuijt@vpro.nl
 */
@FixMethodOrder(NAME_ASCENDING)
@Slf4j
public class SearchWebTest extends AbstractTest {

    public SearchWebTest(@Nonnull WebDriverFactory.Browser browser, @Nonnull String version) {
        super(browser, version);
    }

    /**
     * Sets up. a webdriver connected logged in to Poms interface using SpeciaalVfGebruiken from properties file
     */
    @Test

    public void test00Login() {
        login().VPROand3voor12();
    }

    /**
     * Use the search field to find 'klusjesmannen'
     */
    @Test
    public void test02Search1() {

        // Kies Nieuwe zoekopdracht
        waitForAngularRequestsToFinish();
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


}
