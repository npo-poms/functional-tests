package nl.vpro.poms.selenium.npoapi.thesaurus;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.Select;

import com.fasterxml.jackson.databind.JsonNode;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.gtaa.Scheme;
import nl.vpro.jackson2.Jackson2Mapper;
import nl.vpro.poms.selenium.AbstractTest5;
import nl.vpro.poms.selenium.pages.AbstractLogin;
import nl.vpro.poms.selenium.util.WebDriverFactory.Browser;
import nl.vpro.poms.selenium.util.WebDriverUtil;
import nl.vpro.test.jupiter.AbortOnException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;

/**
 *
 */
@TestMethodOrder(MethodOrderer.Alphanumeric.class)
@Slf4j
@ExtendWith({AbortOnException.class})
public class ThesaurusPopupTest extends AbstractTest5 {

    private static final String EXAMPLE_TITLE = "POMS GTAA";
    private static final String POPUP_TITLE = "GTAA";

    private static Map<Browser, Boolean> loggedIn = new HashMap<>();

    public ThesaurusPopupTest() {

    }

    @Override
    protected AbstractLogin login(Browser browser) {
        String url = CONFIG.getProperties(Config.Prefix.npo_api).get("baseUrl") + "/thesaurus/example/secure";

        return casLogin(url, browser);
    }


    @ParameterizedTest
    @Browsers
    public void test000LoginAndStartPage(Browser browser) {
        if (!loggedIn.getOrDefault(browser, Boolean.FALSE)) {
            login(browser).gtaaBrowserTest();
            browser.getUtil(log).waitForTitle(EXAMPLE_TITLE);
            loggedIn.put(browser, true);
        }
    }

    /**
     * Searches for a person with the Given name: Jan Peter and Family name Balkenende.
     * A list should appear with two values containing "Jan Peter"
     */
    @ParameterizedTest
    @Browsers
    public void test002FindJanPeter(Browser browser) {
        WebDriver driver = browser.getDriver();
        WebDriverUtil webDriverUtil = browser.getUtil(log);
        selectScheme(browser, Scheme.person);

        driver.findElement(id("givenName")).sendKeys("Jan Peter");
        driver.findElement(id("familyName")).sendKeys("Balkenende");
        webDriverUtil.click("open");

        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);
        waitUntilSuggestionReady(browser);

        long counter = driver.findElements(By.xpath("//ul/li"))
                .stream()
                .filter(s -> s.getText().contains("Jan Peter"))
                .count();

        assertThat(counter).isGreaterThanOrEqualTo(2);
    }

    @ParameterizedTest
    @Browsers
    public void test003SelectOne(Browser browser) throws IOException {
        WebDriver driver = browser.getDriver();
        WebDriverUtil webDriverUtil = browser.getUtil(log);

        WebElement jan_peter = driver.findElements(By.xpath("//ul/li"))
                .stream()
                .filter(s -> s.getText().contains("Jan Peter"))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Not found Jan Peter"));
        jan_peter.click();
        webDriverUtil.waitForAngularRequestsToFinish();
        // There should no appear a 'select'

        webDriverUtil.click("submit");

        // Now, the window should disappear
        webDriverUtil.waitForWindowToClose();
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        JsonNode jsonNode = getJson(browser);
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");

    }

    @ParameterizedTest
    @Drivers
    public void test004Geonames(Browser browser) {
        WebDriver driver = browser.getDriver();
        WebDriverUtil webDriverUtil = browser.getUtil(log);

        selectScheme(browser, Scheme.geographicname);
        WebElement name = driver.findElement(id("name"));
        name.clear();
        name.sendKeys("Amsterdam");
        webDriverUtil.click("open");
        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);

        waitUntilSuggestionReady(browser);

        List<WebElement> elements = driver.findElements(By.xpath("//ul/li"));
        Iterator<WebElement> i = elements.iterator();
        WebElement register = null;
        while (i.hasNext()) {
            WebElement e = i.next();
            String[] lines = e.getText().split("\n");
            log.info("{}", Arrays.asList(lines));
            if (i.hasNext()) {
                assertThat(lines[0]).endsWith("[geografische naam]");
            } else {
                register = e;

            }
        }
        assertThat(elements).hasSizeGreaterThan(3);
        assertThat(register).isNotNull();
    }

    @ParameterizedTest
    @Browsers
    public void test005SearchGeoLocationById(Browser browser) throws IOException {
        WebDriverUtil webDriverUtil = browser.getUtil(log);
        WebDriver driver = browser.getDriver();
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        String uriOfAmsterdam = "http://data.beeldengeluid.nl/gtaa/31586";
        webDriverUtil.click("reset");
        driver.findElement(id("id")).sendKeys(uriOfAmsterdam);
        webDriverUtil.click("open");
        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);
        waitUntilSuggestionReady(browser);
        // first suggestion should be it
        WebElement webElement = driver.findElements(By.xpath("//ul/li")).get(0).findElement(tagName("a"));
        assertThat(webElement.getAttribute("id")).isEqualTo(uriOfAmsterdam);
        webElement.click();
        webDriverUtil.click("submit");
        webDriverUtil.waitForWindowToClose();
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        JsonNode jsonNode = getJson(browser);
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");
        assertThat(jsonNode.get("concept").get("id").asText()).isEqualTo(uriOfAmsterdam);
    }

    @ParameterizedTest
    @Browsers
    public void test006RegisterGeoLocation(TestInfo testInfo, Browser browser) throws IOException {
        WebDriverUtil webDriverUtil = browser.getUtil(log);
        WebDriver driver = browser.getDriver();

        webDriverUtil.click("reset");
        selectScheme(browser, Scheme.geographicname);
        webDriverUtil.click("open");
        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);

        String conceptName = testInfo.getDisplayName().replaceAll("[\\[\\]]", "_") + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd'T'HHmmss"));

        search(browser, conceptName);
        WebElement register = driver.findElement(By.className("status-create"));
        register.click();
        webDriverUtil.waitForAngularRequestsToFinish();

        driver.findElement(id("scopeNote")).clear();
        driver.findElement(id("scopeNote")).sendKeys("Made by Selenium test. Don't approve this");
        webDriverUtil.click("register");
        waitForRegistration(browser);

        webDriverUtil.w().until(webdriver -> webdriver.findElement(By.id("submit")));
        webDriverUtil.click("submit");
        webDriverUtil.waitForWindowToClose();
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        JsonNode jsonNode = getJson(browser);
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");
        assertThat(jsonNode.get("concept").get("status").asText()).isEqualTo("candidate");
        assertThat(jsonNode.get("concept").get("name").asText()).isEqualTo(conceptName);
        assertThat(jsonNode.get("concept").get("scopeNotes").get(0).asText()).isNotEmpty();
    }

    @ParameterizedTest
    @Browsers
    public void test007OpenModal(Browser browser) throws IOException {
        WebDriver driver = browser.getDriver();
        WebDriverUtil webDriverUtil = browser.getUtil(log);

        selectScheme(browser, Scheme.geographicname);
        WebElement name = driver.findElement(id("name"));
        name.clear();
        name.sendKeys("Amsterdam");
        webDriverUtil.click("openmodal");
        webDriverUtil.waitForAngularRequestsToFinish();
        driver.switchTo().frame("iframe");
        webDriverUtil.waitForAngularRequestsToFinish();
        waitUntilSuggestionReady(browser);
        WebElement webElement = driver.findElements(By.xpath("//ul/li")).get(0).findElement(tagName("a"));
        assertThat(webElement.getAttribute("id")).startsWith("http://data");
        webElement.click();
        webDriverUtil.click("submit");
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        driver.switchTo().defaultContent();
        JsonNode jsonNode = getJson(browser);
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");
        assertThat(jsonNode.get("concept").get("name").asText()).containsIgnoringCase("amsterdam");
    }

    @ParameterizedTest
    @Browsers
    public void test008MultipleSchemes(Browser browser) throws IOException {
        WebDriver driver = browser.getDriver();
        WebDriverUtil webDriverUtil = browser.getUtil(log);

        selectScheme(browser, Scheme.geographicname, Scheme.person);
        WebElement name = driver.findElement(id("name"));
        name.clear();
        name.sendKeys("Amsterdam");
        webDriverUtil.click("openmodal");
        webDriverUtil.waitForAngularRequestsToFinish();
        driver.switchTo().frame("iframe");
        webDriverUtil.waitForAngularRequestsToFinish();
        waitUntilSuggestionReady(browser);
        WebElement webElement = driver.findElements(By.xpath("//ul/li")).get(0).findElement(tagName("a"));
        assertThat(webElement.getAttribute("id")).startsWith("http://data");
        webElement.click();
        webDriverUtil.click("submit");
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        driver.switchTo().defaultContent();
        JsonNode jsonNode = getJson(browser);
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");
        assertThat(jsonNode.get("concept").get("name").asText()).containsIgnoringCase("amsterdam");
    }


    private void search(Browser browser, String value) {
        WebElement searchValue = browser.getUtil(log).w().until(driver -> driver.findElement(id("searchValue")));
        searchValue.clear();
        searchValue.sendKeys(value);
        waitUntilSuggestionReady(browser);
    }

    private void waitUntilSuggestionReady(Browser browser) {
        browser.getUtil(log).w().until(webDriver ->
            !webDriver.findElement(id("searchValue")).getAttribute("class").contains("waiting"));
    }

    private void waitForRegistration(Browser browser) {
        browser.getUtil(log).w().until(webDriver -> {
            try {
                webDriver.findElement(id("spinner")).findElement(tagName("img"));
                return true;
            } catch (NoSuchElementException nsee) {
                return false;
            }
        });
        browser.getUtil(log).waitForAngularRequestsToFinish();
    }

    private void selectScheme(Browser browser, Scheme... scheme) {
        WebDriverUtil webDriverUtil = browser.getUtil(log);
        webDriverUtil.waitForAngularRequestsToFinish();
        WebElement schemes = browser.getDriver().findElement(id("schemes"));
        Select select = new Select(schemes);
        select.deselectAll();
        for (Scheme s : scheme) {
            select.selectByValue(s.name());
        }
        webDriverUtil.waitForAngularRequestsToFinish();
    }

    private JsonNode getJson(Browser browser) throws IOException {
        WebDriverUtil webDriverUtil = browser.getUtil(log);
        WebDriver driver = browser.getDriver();
        webDriverUtil.waitForAngularRequestsToFinish();
        WebElement jsonArea = driver.findElement(id("json"));
        String json = jsonArea.getAttribute("value");

        return Jackson2Mapper.getLenientInstance().readTree(new StringReader(json));
    }
}
