package nl.vpro.poms.selenium.poms.maken;


import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;


/**
 * Selenium testcases to test Creating media objects.
 *
 *
 * @author e.kuijt@vpro.nl
 */
@FixMethodOrder(NAME_ASCENDING)
@Slf4j
public class CreateUserWebTest extends AbstractTest {

    public CreateUserWebTest(WebDriverFactory.Browser browser, String version) {
        super(browser, version);
    }

    /**
     * Sets up. a webdriver connected logged in to Poms interface using SpeciaalVfGebruiken from properties file
     */
    @Test
    public void test00login() {
        login().VPROand3voor12();
    }

    /**
     * Create a clip, forgetting to fill in the genre.
     *
     * Nog in te vullen: Genre should be in the footer of the page
     */
    @Test
    public void createUser1() {

        // Kies Nieuw
        waitForAngularRequestsToFinish();
        driver.findElement(By.linkText("NIEUW")).click();

        // Kies media type "Clip maar vul het formulier onvolledig in, er ontbreken nog verplichte(*) velden
        driver.findElement(By.id("inputTitle")).sendKeys("Selenium test clip");

        // Select Serie from Media type
        WebElement mediaType = driver.findElement(By.name("Media Type *"));
        mediaType.click();
        mediaType.findElements(By.cssSelector("div[ng-click]"))
                .stream()
                .filter(webElement -> webElement.getText().contains("Clip"))
                .collect(Collectors.toList())
                .get(0)
                .click();

        //Select Video from Media type
        WebElement avType = driver.findElement(By.name("AV Type *"));
        avType.click();
        avType.findElements(By.cssSelector("div[ng-bind-html]"))
                .stream()
                .filter(webElement -> webElement.getText().contains("Video"))
                .collect(Collectors.toList())
                .get(0)
                .click();

        // Select List in footer remove "Nog in te vullen" and Collect in array
        String[] missing_list = driver.findElement(By.cssSelector("div[class='required-feedback footer-message']"))
                .findElements(By.tagName("span"))
                .stream()
                .map(WebElement::getText)
                .filter(s -> !s.contains("Nog in te vullen"))
                .toArray(String[]::new);

        assertArrayEquals(new String[]{"Genre"}, missing_list);

    }

    /**
     * Now on the same page at Genre, Jeugd is filled in, the form is submitted and the media object generated should
     * have a MID, URN, status should be 'Voor publicatie', the selected tab should be shaded.
     */
    @Test
    public void createUser2() {
        //Select Jeugd from Genre type
        WebElement genre = driver.findElement(By.name("Genre *"));
        genre.click();
        genre.findElements(By.cssSelector("div[ng-bind-html]"))
                .stream()
                .filter(webElement -> webElement.getText().contains("Jeugd"))
                .collect(Collectors.toList())
                .get(0)
                .click();

        driver.findElement(By.cssSelector("button[ng-click='controller.submit()']")).click();

        //WebElement clipboard = driver.findElement(By.cssSelector("input[type='text' ng-model='formData.text']"));

        // find MID, URN here


    }

}
