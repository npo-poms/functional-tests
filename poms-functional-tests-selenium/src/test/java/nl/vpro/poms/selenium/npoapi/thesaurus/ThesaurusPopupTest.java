package nl.vpro.poms.selenium.npoapi.thesaurus;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import com.fasterxml.jackson.databind.JsonNode;

import nl.vpro.api.client.utils.Config;
import nl.vpro.domain.gtaa.Scheme;
import nl.vpro.jackson2.Jackson2Mapper;
import nl.vpro.poms.selenium.poms.AbstractTest;
import nl.vpro.poms.selenium.util.WebDriverFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeNoException;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;
import static org.openqa.selenium.By.id;
import static org.openqa.selenium.By.tagName;

/**
 *
 */
@FixMethodOrder(NAME_ASCENDING)
@Slf4j
public class ThesaurusPopupTest extends AbstractTest {

    private static final String EXAMPLE_TITLE = "POMS GTAA";
    private static final String POPUP_TITLE = "GTAA";

    private static boolean loggedIn = false;
    @Before
    public void setup() {
        assumeNoException(exceptions.get(ThesaurusPopupTest.class));
    }

    public ThesaurusPopupTest(@Nonnull WebDriverFactory.Browser browser) {
        super(browser);
    }

    @Before
    public void test000login() {
        if (! loggedIn) {
            String url = CONFIG.getProperties(Config.Prefix.npo_api).get("baseUrl") + "/thesaurus/example/";
            login(url).gtaaBrowserTest();
            webDriverUtil.waitForTitle(EXAMPLE_TITLE);
            loggedIn = true;
        }
    }

    /**
     * Searches for a person with the Given name: Jan Peter and Family name Balkenende.
     * A list should appear with two values containing "Jan Peter"

     */
    @Test
    public void test002FindJanPeter() {
        selectScheme(Scheme.person);

        driver.findElement(id("givenName")).sendKeys("Jan Peter");
        driver.findElement(id("familyName")).sendKeys("Balkenende");
        webDriverUtil.click("open");

        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);
        waitUntilSuggestionReady();

        long counter = driver.findElements(By.xpath("//ul/li"))
                .stream()
                .filter(s -> s.getText().contains("Jan Peter"))
                .count();

        assertThat(counter).isGreaterThanOrEqualTo(2);
    }

    @Test
    public void test003SelectOne() throws  IOException {

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
        JsonNode jsonNode = getJson();
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");

    }

    @Test
    public void test004Geonames() {
        selectScheme(Scheme.geographicname);
        WebElement name = driver.findElement(id("name"));
        name.clear();
        name.sendKeys("Amsterdam");
        webDriverUtil.click("open");
        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);

        waitUntilSuggestionReady();

        List<WebElement> elements = driver.findElements(By.xpath("//ul/li"));
        Iterator<WebElement> i = elements.iterator();
        WebElement register = null;
        while(i.hasNext()) {
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

    @Test
    public void test005RegisterGeonames() throws IOException {
        String conceptName = testMethod.getMethodName().replaceAll("[\\[\\]]", "_") + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd'T'HHmmss"));

        List<WebElement> e  =  search(conceptName);
        WebElement register = e.get(e.size() -1);
        register.click();
        webDriverUtil.waitForAngularRequestsToFinish();

        driver.findElement(id("scopeNote")).clear();
        driver.findElement(id("scopeNote")).sendKeys("Made by Selenium test. Don't approve this");
        webDriverUtil.click("register");
        waitForRegistration();

        webDriverUtil.click("submit");
        webDriverUtil.waitForWindowToClose();
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        JsonNode jsonNode = getJson();
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");
        assertThat(jsonNode.get("concept").get("status").asText()).isEqualTo("candidate");
        assertThat(jsonNode.get("concept").get("name").asText()).isEqualTo(conceptName);
        assertThat(jsonNode.get("concept").get("scopeNotes").get(0).asText()).isNotEmpty();
    }

    @Test
    public void test006SearchById() throws IOException {
        String uriOfAmsterdam = "http://data.beeldengeluid.nl/gtaa/31586";
        webDriverUtil.click("reset");
        driver.findElement(id("id")).sendKeys(uriOfAmsterdam);
        webDriverUtil.click("open");
        webDriverUtil.waitForAngularRequestsToFinish();
        webDriverUtil.switchToWindowWithTitle(POPUP_TITLE);
        waitUntilSuggestionReady();
        // first suggestion should be it
        WebElement webElement = driver.findElements(By.xpath("//ul/li")).get(0).findElement(tagName("a"));
        assertThat(webElement.getAttribute("id")).isEqualTo(uriOfAmsterdam);
        webElement.click();
        webDriverUtil.click("submit");
        webDriverUtil.waitForWindowToClose();
        webDriverUtil.switchToWindowWithTitle(EXAMPLE_TITLE);
        JsonNode jsonNode = getJson();
        assertThat(jsonNode.get("action").asText()).isEqualTo("selected");
        assertThat(jsonNode.get("concept").get("id").asText()).isEqualTo(uriOfAmsterdam);
    }

    private List<WebElement> search(String value){
        WebElement searchValue = driver.findElement(id("searchValue"));
        searchValue.clear();
        searchValue.sendKeys(value);
        waitUntilSuggestionReady();
        return driver.findElements(By.xpath("//ul/li"));
    }
    private void waitUntilSuggestionReady() {
        wait.until(webDriver ->
                ! webDriver.findElement(id("searchValue")).getAttribute("class").contains("waiting"));
    }
    private void waitForRegistration() {
        wait.until(webDriver -> {
            try {
                webDriver.findElement(id("spinner")).findElement(tagName("img"));
                return true;
            } catch (NoSuchElementException nsee) {
                return false;
            }
        });
        webDriverUtil.waitForAngularRequestsToFinish();
    }
    private void selectScheme(Scheme... scheme) {
        webDriverUtil.waitForAngularRequestsToFinish();
        WebElement schemes = driver.findElement(id("schemes"));
        Select select = new Select(schemes);
        select.deselectAll();
        for (Scheme s : scheme) {
            select.selectByValue(s.name());
        }
        webDriverUtil.waitForAngularRequestsToFinish();
    }
    private JsonNode getJson() throws IOException {
        webDriverUtil.waitForAngularRequestsToFinish();
        WebElement jsonArea = driver.findElement(id("json"));
        String json = jsonArea.getAttribute("value");

        return Jackson2Mapper.getLenientInstance().readTree(new StringReader(json));
    }


}
